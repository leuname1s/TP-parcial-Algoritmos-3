package Controladores;

import Funcionalidades.ServicioEstadisticas;
import Interfaces.IPartida;
import Interfaces.IVistaJuego;
import Controladores.EstadoVistaJuego.Guardado;
import datos.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Coordina presentación y persistencia; las reglas y las decisiones siguen en el motor. */
public final class ControladorJuego {
    /** Un solo aviso pendiente para poder cancelar el turno programado. */
    public interface RelojTurnos {
        void programar(int demora, Runnable accion);
        void cancelar();
    }

    private static final class RelojSwing implements RelojTurnos {
        private Timer timer;
        @Override public void programar(int demora, Runnable accion) {
            cancelar();
            timer = new Timer(demora, evento -> accion.run());
            timer.setRepeats(false);
            timer.start();
        }
        @Override public void cancelar() { if (timer != null) { timer.stop(); timer = null; } }
    }

    private final IPartida partida;
    private final ServicioEstadisticas servicio;
    private final IVistaJuego vista;
    private final RelojTurnos reloj;
    private final Executor trabajo;
    private final Consumer<Runnable> volverAlHiloVista;
    private final ExecutorService ejecutorPropio;
    private final List<TurnoRegistrado> historial = new ArrayList<>();
    private String usuario = "";
    private boolean menu = true;
    private boolean cerrado;
    private boolean pausado;
    private boolean accionEnCurso;
    private int demoraMaquina = 900;
    private long sesion;
    private long avisoTurno;
    private long consulta;
    private Guardado guardado = Guardado.NO_CORRESPONDE;
    private Estadisticas estadisticasUsuario;
    private Estadisticas estadisticasMaquinas;
    private boolean cargandoEstadisticas;
    private String errorEstadisticas;
    private String errorGuardado;

    public ControladorJuego(IPartida partida, ServicioEstadisticas servicio, IVistaJuego vista) {
        this(partida, servicio, vista, new RelojSwing(), crearEjecutor(), SwingUtilities::invokeLater, true);
    }

    /** Permite proporcionar el reloj, el ejecutor y el despacho hacia la vista. */
    public ControladorJuego(IPartida partida, ServicioEstadisticas servicio, IVistaJuego vista,
            RelojTurnos reloj, Executor trabajo, Consumer<Runnable> volverAlHiloVista) {
        this(partida, servicio, vista, reloj, trabajo, volverAlHiloVista, false);
    }

    private ControladorJuego(IPartida partida, ServicioEstadisticas servicio, IVistaJuego vista,
            RelojTurnos reloj, Executor trabajo, Consumer<Runnable> volverAlHiloVista, boolean propio) {
        this.partida = Objects.requireNonNull(partida);
        this.servicio = Objects.requireNonNull(servicio);
        this.vista = Objects.requireNonNull(vista);
        this.reloj = Objects.requireNonNull(reloj);
        this.trabajo = Objects.requireNonNull(trabajo);
        this.volverAlHiloVista = Objects.requireNonNull(volverAlHiloVista);
        this.ejecutorPropio = propio ? (ExecutorService) trabajo : null;
    }

    private static ExecutorService crearEjecutor() {
        // Serializar lectura-modificación-escritura evita perder resultados entre tareas de esta ventana.
        return Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "estadisticas-adivina-quien");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    public void abrir() {
        if (!cerrado) { publicar(); cargarEstadisticas(); }
    }

    public void consultarEstadisticas(String nombre) {
        if (cerrado || !menu) { return; }
        usuario = nombre == null ? "" : nombre.strip();
        cargarEstadisticas();
    }

    public void iniciarPartida(String nombre, ModoJuego modo) {
        if (cerrado || !menu) { return; }
        String nuevoUsuario = nombre == null ? "" : nombre.strip();
        if (modo != ModoJuego.MAQUINA_1_VS_MAQUINA_2 && nuevoUsuario.isEmpty()) {
            vista.mostrarError("Ingresá un nombre para jugar y registrar tus estadísticas.");
            return;
        }
        try {
            partida.iniciar(modo);
        } catch (IllegalArgumentException ex) {
            vista.mostrarError(ex.getMessage());
            return;
        }
        cancelarTurnoPendiente();
        sesion++;
        usuario = nuevoUsuario;
        menu = false;
        pausado = false;
        historial.clear();
        guardado = Guardado.NO_CORRESPONDE;
        errorGuardado = null;
        publicar();
        cargarEstadisticas();
        programarMaquina();
    }

    public void seleccionarPersonaje(int id) {
        seleccionar(() -> partida.seleccionarPersonaje(id));
    }

    public void seleccionarPersonajeAleatorio() {
        seleccionar(partida::seleccionarPersonajeAleatorio);
    }

    private void seleccionar(Runnable accion) {
        if (!enEstado(EstadoPartida.SELECCION_PERSONAJE) || accionEnCurso) { return; }
        try {
            accion.run();
            publicar();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    public void preguntar(Pregunta pregunta) {
        if (turnoHumano()) { resolverAccion(() -> partida.preguntar(pregunta)); }
    }

    public void arriesgar(int id) {
        if (turnoHumano()) { resolverAccion(() -> partida.arriesgar(id)); }
    }

    private boolean turnoHumano() {
        return enEstado(EstadoPartida.EN_CURSO) && partida.getTurno() == Participante.JUGADOR;
    }

    private boolean enEstado(EstadoPartida estado) {
        return !cerrado && !menu && partida.getEstado() == estado;
    }

    private void resolverAccion(Supplier<ResultadoTurno> accion) {
        if (accionEnCurso || cerrado || menu) { return; }
        accionEnCurso = true;
        int fase = partida.getFase();
        int ronda = partida.getRonda();
        try {
            ResultadoTurno resultado = accion.get();
            historial.add(new TurnoRegistrado(fase, ronda, resultado));
            cancelarTurnoPendiente();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            vista.mostrarError(ex.getMessage());
            return;
        } finally {
            accionEnCurso = false;
        }
        despuesDeAccion();
    }

    public void decidirSegundaFase(boolean aceptar) {
        if (!enEstado(EstadoPartida.DECISION_SEGUNDA_FASE)) { return; }
        partida.decidirSegundaFase(aceptar);
        despuesDeAccion();
    }

    private void despuesDeAccion() {
        if (partida.getEstado() == EstadoPartida.FINALIZADA) {
            cancelarTurnoPendiente();
            guardarResultado();
        } else {
            publicar();
            programarMaquina();
        }
    }

    private void programarMaquina() {
        cancelarTurnoPendiente();
        if (!enEstado(EstadoPartida.EN_CURSO) || partida.getTurno() == Participante.JUGADOR || pausado) {
            return;
        }
        long aviso = avisoTurno;
        // Un evento ya encolado también debe quedar invalidado al pausar, salir o iniciar otra partida.
        reloj.programar(demoraMaquina, () -> {
            if (aviso != avisoTurno || pausado || !enEstado(EstadoPartida.EN_CURSO)
                    || partida.getTurno() == Participante.JUGADOR) { return; }
            resolverAccion(partida::ejecutarTurnoMaquina);
        });
    }

    private void cancelarTurnoPendiente() { avisoTurno++; reloj.cancelar(); }

    public void alternarPausa() {
        if (!espectadorActivo()) { return; }
        pausado = !pausado;
        publicar();
        programarMaquina();
    }

    public void siguienteTurno() {
        if (!espectadorActivo() || !pausado) { return; }
        cancelarTurnoPendiente();
        resolverAccion(partida::ejecutarTurnoMaquina);
    }

    public void cambiarDemora(int milisegundos) {
        if (milisegundos < 250 || milisegundos > 5000 || cerrado) { return; }
        demoraMaquina = milisegundos;
        publicar();
        programarMaquina();
    }

    private boolean espectadorActivo() {
        return enEstado(EstadoPartida.EN_CURSO) && partida.getModo() == ModoJuego.MAQUINA_1_VS_MAQUINA_2;
    }

    public void reintentarGuardado() {
        if (enEstado(EstadoPartida.FINALIZADA) && guardado == Guardado.ERROR) { guardarResultado(); }
    }

    private void guardarResultado() {
        if (guardado == Guardado.GUARDANDO || guardado == Guardado.GUARDADO) { return; }
        ResultadoPartida resultado = partida.getResultado();
        if (resultado == null) { return; }
        final String nombre = usuario;
        final long sesionGuardada = sesion;
        guardado = Guardado.GUARDANDO;
        errorGuardado = null;
        publicar();
        // Capturar resultado y usuario impide registrar una partida con datos de una sesión posterior.
        enSegundoPlano(() -> servicio.registrar(nombre, resultado), (registrado, error) -> {
            if (cerrado || sesion != sesionGuardada) { return; }
            guardado = error == null ? Guardado.GUARDADO : Guardado.ERROR;
            errorGuardado = error == null ? null : mensaje(error);
            publicar();
            if (error == null) { cargarEstadisticas(); }
        });
    }

    private void cargarEstadisticas() {
        long numeroConsulta = ++consulta;
        String nombre = usuario;
        cargandoEstadisticas = true;
        errorEstadisticas = null;
        estadisticasUsuario = null;
        estadisticasMaquinas = null;
        publicar();
        enSegundoPlano(() -> new Estadisticas[] {
                nombre.isEmpty() ? null : servicio.consultarUsuario(nombre), servicio.consultarMaquinas()
        }, (valores, error) -> {
            if (cerrado || numeroConsulta != consulta) { return; }
            cargandoEstadisticas = false;
            if (error == null) {
                estadisticasUsuario = valores[0];
                estadisticasMaquinas = valores[1];
            } else {
                errorEstadisticas = mensaje(error);
            }
            publicar();
        });
    }

    private <T> void enSegundoPlano(Callable<T> accion, BiConsumer<T, Exception> recibir) {
        trabajo.execute(() -> {
            T resultado = null;
            Exception fallo = null;
            try { resultado = accion.call(); } catch (Exception ex) { fallo = ex; }
            T respuesta = resultado;
            Exception error = fallo;
            volverAlHiloVista.accept(() -> recibir.accept(respuesta, error));
        });
    }

    public void volverAlMenu() {
        if (cerrado || menu || !autorizaNavegacion()) { return; }
        sesion++;
        menu = true;
        pausado = false;
        guardado = Guardado.NO_CORRESPONDE;
        errorGuardado = null;
        historial.clear();
        publicar();
        cargarEstadisticas();
    }

    public void solicitarSalir() {
        if (cerrado || !autorizaNavegacion()) { return; }
        cerrado = true;
        consulta++;
        sesion++;
        cancelarTurnoPendiente();
        if (ejecutorPropio != null) { ejecutorPropio.shutdown(); }
        vista.cerrar();
    }

    private boolean autorizaNavegacion() {
        cancelarTurnoPendiente();
        if (guardado == Guardado.GUARDANDO) {
            vista.mostrarError("Se está guardando el resultado. Esperá a que termine para salir.");
            return false;
        }
        boolean aceptar = true;
        if (!menu && partida.getEstado() != EstadoPartida.FINALIZADA) {
            aceptar = vista.confirmarAbandono();
        } else if (!menu && guardado == Guardado.ERROR) {
            aceptar = vista.confirmarSalidaSinGuardar();
        }
        if (!aceptar) { programarMaquina(); }
        return aceptar;
    }

    private static String mensaje(Exception error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private void publicar() {
        if (cerrado) { return; }
        Map<Participante, Set<Integer>> candidatos = new EnumMap<>(Participante.class);
        Map<Participante, Personaje> secretos = new EnumMap<>(Participante.class);
        boolean espectador = partida.getModo() == ModoJuego.MAQUINA_1_VS_MAQUINA_2;
        if (!menu) {
            for (Participante participante : Participante.values()) {
                Set<Integer> ids = new HashSet<>();
                for (Personaje personaje : partida.getCandidatos(participante)) { ids.add(personaje.getId()); }
                candidatos.put(participante, ids);
                if (espectador && participante != Participante.JUGADOR) {
                    secretos.put(participante, partida.getSecretoEspectador(participante));
                }
            }
        }
        Personaje secretoFinal = !menu && !espectador && partida.getEstado() == EstadoPartida.FINALIZADA
                ? partida.getSecretoRivalFinal() : null;
        vista.actualizar(new EstadoVistaJuego(menu, usuario, menu ? EstadoPartida.SIN_INICIAR : partida.getEstado(),
                menu ? null : partida.getModo(), menu ? null : partida.getTurno(), menu ? null : partida.getRivalActual(),
                partida.getFase(), partida.getRonda(), menu ? List.of() : partida.getPersonajes(), candidatos,
                menu ? null : partida.getPersonajeJugador(), secretos, secretoFinal,
                menu ? Set.of() : partida.getPreguntasRealizadas(), historial, menu ? null : partida.getResultado(),
                pausado, demoraMaquina, guardado, estadisticasUsuario, estadisticasMaquinas,
                cargandoEstadisticas, errorEstadisticas, errorGuardado));
    }
}
