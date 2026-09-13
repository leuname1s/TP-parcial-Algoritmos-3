package Controladores;

import Controladores.EstadoVistaJuego.Guardado;
import Funcionalidades.Partida;
import Funcionalidades.ServicioEstadisticas;
import Interfaces.IRepositorioEstadisticas;
import Interfaces.IVistaJuego;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoPartida;
import datos.ResultadoPartida.Desenlace;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;
import static soporte.PartidaPrueba.rechaza;

/** Verifica coordinación y carreras de callbacks sin archivos, ventanas ni esperas reales. */
public final class ControladorJuegoRegressionTest {
    private ControladorJuegoRegressionTest() { }

    public static void main(String[] args) throws IOException {
        entradasEInstantaneas();
        relojYNavegacion();
        fasesYResultados();
        desenlacesHumanos();
        erroresYReintentos();
        estadisticasYRespuestasTardias();
        abandonoSinRegistro();
        System.out.println("PASS: controlador, turnos, cancelaciones, fases, persistencia y respuestas tardías");
    }

    private static void entradasEInstantaneas() {
        Escenario caso = new Escenario();
        caso.controlador.abrir();
        caso.drenar();
        caso.controlador.iniciarPartida("  ", ModoJuego.JUGADOR_VS_MAQUINA_1);
        comprobar(caso.estado().menu() && caso.vista.errores.size() == 1, "Aceptó usuario vacío");
        caso.controlador.iniciarPartida("Ana", null);
        comprobar(caso.estado().menu() && caso.vista.errores.size() == 2, "Aceptó modo nulo");
        caso.controlador.iniciarPartida("  Ana  ", ModoJuego.JUGADOR_VS_MAQUINA_1);
        caso.drenar();
        comprobar(caso.estado().usuario().equals("Ana"), "No normalizó el nombre");
        comprobar(caso.estado().estado() == EstadoPartida.SELECCION_PERSONAJE, "Falta selección");
        caso.controlador.preguntar(Pregunta.USA_LENTES);
        caso.controlador.arriesgar(caso.prueba.primerSecreto.getId());
        caso.controlador.seleccionarPersonaje(-1);
        comprobar(caso.estado().historial().isEmpty(), "Selección inválida consumió un turno");
        comprobar(caso.estado().estado() == EstadoPartida.SELECCION_PERSONAJE, "Cambió el estado inválido");
        caso.controlador.seleccionarPersonaje(caso.prueba.humano.getId());
        EstadoVistaJuego inicial = caso.estado();
        caso.controlador.seleccionarPersonajeAleatorio();
        comprobar(caso.estado().secretoJugador().equals(inicial.secretoJugador()), "Doble selección modificó secreto");
        comprobar(inicial.secretoRivalFinal() == null && inicial.secretosEspectador().isEmpty(), "Filtró secreto rival");
        comprobar(inicial.candidatosDe(Participante.JUGADOR).size() == 23, "Tablero inicial incorrecto");
        rechaza(UnsupportedOperationException.class, () -> inicial.personajes().clear());
        rechaza(UnsupportedOperationException.class, () -> inicial.candidatos().clear());
        rechaza(UnsupportedOperationException.class, () -> inicial.candidatosDe(Participante.JUGADOR).clear());
        rechaza(UnsupportedOperationException.class, () -> inicial.historial().clear());
        rechaza(UnsupportedOperationException.class, () -> inicial.preguntasRealizadas().add(Pregunta.USA_LENTES));
        rechaza(UnsupportedOperationException.class, () -> inicial.secretosEspectador().put(
                Participante.MAQUINA_1, caso.prueba.primerSecreto));
        caso.controlador.preguntar(null);
        caso.controlador.arriesgar(-1);
        comprobar(caso.estado().turnoHumano() && caso.estado().historial().isEmpty(), "Entrada inválida consumió turno");
        caso.controlador.preguntar(Pregunta.USA_LENTES);
        comprobar(caso.estado().historial().size() == 1, "No registró pregunta válida");
        comprobar(inicial.historial().isEmpty() && inicial.preguntasRealizadas().isEmpty()
                && inicial.candidatosDe(Participante.JUGADOR).size() == 23, "Mutó una instantánea anterior");
        caso.controlador.preguntar(Pregunta.USA_LENTES);
        caso.controlador.arriesgar(caso.prueba.primerSecreto.getId());
        comprobar(caso.estado().historial().size() == 1, "Doble evento humano consumió otro turno");
        Runnable turno = caso.reloj.pendiente;
        caso.reloj.disparar();
        turno.run();
        comprobar(caso.estado().historial().size() == 2 && caso.estado().ronda() == 2,
                "Callback duplicado ejecutó más de un turno");
        TurnoRegistrado registrado = caso.estado().historial().get(1);
        comprobar(registrado.ronda() == 1 && registrado.fase() == 1, "Historial usó ronda posterior a la acción");
        int errores = caso.vista.errores.size();
        caso.controlador.preguntar(Pregunta.USA_LENTES);
        comprobar(caso.vista.errores.size() == errores + 1 && caso.estado().historial().size() == 2
                && caso.estado().turnoHumano(), "Pregunta repetida alteró el turno");
        int descartado = caso.estado().personajes().stream()
                .filter(p -> !caso.estado().candidatosDe(Participante.JUGADOR).contains(p.getId()))
                .findFirst().orElseThrow().getId();
        caso.controlador.arriesgar(descartado);
        comprobar(caso.estado().historial().size() == 2, "Permitió arriesgar un descartado");
    }

    private static void relojYNavegacion() {
        Escenario caso = new Escenario();
        caso.iniciar("", ModoJuego.MAQUINA_1_VS_MAQUINA_2);
        comprobar(caso.estado().secretosEspectador().size() == 2, "Espectador sin ambos secretos");
        Runnable inicial = caso.reloj.pendiente;
        caso.controlador.siguienteTurno();
        comprobar(caso.estado().historial().isEmpty(), "Siguiente turno funcionó sin pausa");
        caso.controlador.alternarPausa();
        inicial.run();
        comprobar(caso.estado().pausado() && caso.estado().historial().isEmpty(), "Pausa dejó ejecutar evento pendiente");
        caso.controlador.siguienteTurno();
        comprobar(caso.estado().historial().size() == 1 && caso.reloj.pendiente == null,
                "Un paso manual programó más turnos");
        caso.controlador.cambiarDemora(350);
        comprobar(caso.estado().demoraMaquina() == 350 && caso.reloj.pendiente == null, "Velocidad levantó pausa");
        caso.controlador.cambiarDemora(1);
        comprobar(caso.estado().demoraMaquina() == 350, "Aceptó velocidad fuera de rango");
        caso.controlador.alternarPausa();
        Runnable reanudado = caso.reloj.pendiente;
        comprobar(caso.reloj.demora == 350, "Reanudó con velocidad anterior");
        caso.controlador.cambiarDemora(500);
        reanudado.run();
        comprobar(caso.estado().historial().size() == 1, "Cambio de velocidad conservó callback viejo");
        Runnable antesDelDialogo = caso.reloj.pendiente;
        caso.vista.duranteConfirmacion = antesDelDialogo;
        caso.vista.abandonar = false;
        caso.controlador.volverAlMenu();
        comprobar(!caso.estado().menu() && caso.estado().historial().size() == 1
                && caso.reloj.pendiente != null, "Cancelar abandono no preservó la partida y el reloj");
        Runnable antesDeSalir = caso.reloj.pendiente;
        caso.vista.abandonar = true;
        caso.controlador.volverAlMenu();
        antesDeSalir.run();
        comprobar(caso.estado().menu() && caso.estado().historial().isEmpty(), "Callback alcanzó menú");
        caso.controlador.iniciarPartida("", ModoJuego.MAQUINA_1_VS_MAQUINA_2);
        antesDeSalir.run();
        comprobar(caso.estado().historial().isEmpty(), "Callback de otra sesión ejecutó turno");
        Runnable antesDeCerrar = caso.reloj.pendiente;
        caso.controlador.solicitarSalir();
        int actualizaciones = caso.vista.actualizaciones;
        antesDeCerrar.run();
        caso.drenar();
        comprobar(caso.vista.cerrada && caso.vista.actualizaciones == actualizaciones,
                "Una respuesta tardía actualizó la ventana cerrada");
        comprobar(caso.repo.escrituras == 0, "Espectador incompleto contó como partida");
    }

    private static void fasesYResultados() {
        for (ModoJuego modo : modosHumanos()) {
            Escenario caso = new Escenario();
            caso.iniciarHumano("Ana", modo);
            caso.fallarJugador();
            caso.reloj.disparar();
            caso.controlador.preguntar(Pregunta.USA_LENTES);
            caso.reloj.disparar();
            caso.controlador.arriesgar(caso.prueba.primerSecreto.getId());
            comprobar(caso.estado().estado() == EstadoPartida.DECISION_SEGUNDA_FASE,
                    "No ofreció segunda fase");
            comprobar(caso.estado().resultado() == null && caso.estado().secretoRivalFinal() == null
                    && caso.estado().guardado() == Guardado.NO_CORRESPONDE && caso.io.tareas.isEmpty(),
                    "Victoria intermedia filtró secreto o se intentó guardar");
            caso.controlador.decidirSegundaFase(true);
            comprobar(caso.estado().fase() == 2 && caso.estado().ronda() == 1 && caso.estado().turnoHumano(),
                    "Segunda fase no reinició ronda y turno");
            comprobar(caso.estado().secretoJugador().equals(caso.prueba.humano)
                    && !caso.estado().candidatosDe(Participante.JUGADOR).contains(caso.prueba.primerSecreto.getId())
                    && caso.estado().preguntasRealizadas().isEmpty(), "Segunda fase no conservó/resetó datos requeridos");
            comprobar(caso.estado().rival() == (modo == ModoJuego.JUGADOR_VS_MAQUINA_1
                    ? Participante.MAQUINA_2 : Participante.MAQUINA_1), "No cambió rival");
            caso.controlador.arriesgar(caso.prueba.segundoSecreto.getId());
            comprobar(caso.estado().resultado().getDesenlace() == Desenlace.VICTORIA_VERDADERA,
                    "No registró victoria verdadera");
            comprobar(caso.estado().secretoRivalFinal().equals(caso.prueba.segundoSecreto), "Reveló secreto de fase anterior");
            TurnoRegistrado ultimo = caso.estado().historial().get(caso.estado().historial().size() - 1);
            comprobar(ultimo.fase() == 2 && ultimo.ronda() == 1, "Historial final sin fase correcta");
            comprobar(caso.estado().guardado() == Guardado.GUARDANDO && caso.io.tareas.size() == 1,
                    "Resultado sin un único guardado pendiente");
            caso.controlador.decidirSegundaFase(false);
            caso.controlador.reintentarGuardado();
            caso.controlador.arriesgar(caso.prueba.segundoSecreto.getId());
            comprobar(caso.io.tareas.size() == 1, "Eventos finales duplicaron guardado");
            caso.controlador.volverAlMenu();
            caso.controlador.solicitarSalir();
            comprobar(!caso.estado().menu() && !caso.vista.cerrada && caso.vista.confirmacionesAbandono == 0,
                    "Permitió navegar durante guardado");
            caso.drenar();
            comprobar(caso.repo.escrituras == 1 && caso.estado().guardado() == Guardado.GUARDADO,
                    "No guardó exactamente una vez");
            comprobar(caso.estado().estadisticasUsuario().getVictoriasVerdaderas() == 1,
                    "No refrescó estadísticas tras guardar");
            caso.controlador.volverAlMenu();
            caso.drenar();
            comprobar(caso.estado().menu() && caso.vista.confirmacionesAbandono == 0,
                    "Resultado guardado pidió abandono");
        }
        Escenario espectador = new Escenario();
        espectador.iniciar("", ModoJuego.MAQUINA_1_VS_MAQUINA_2);
        for (int i = 0; i < 46 && espectador.estado().estado() != EstadoPartida.FINALIZADA; i++) {
            espectador.reloj.disparar();
        }
        comprobar(espectador.estado().estado() == EstadoPartida.FINALIZADA, "Espectador no terminó");
        espectador.drenar();
        comprobar(espectador.repo.escrituras == 1 && espectador.estado().estadisticasMaquinas().getPartidasTotales() == 1,
                "No registró resultado de máquinas");
        comprobar(espectador.estado().estadisticasUsuario() == null, "Inventó usuario del espectador");
    }

    private static void erroresYReintentos() {
        Escenario caso = new Escenario();
        caso.iniciarHumano(" Ana ", ModoJuego.JUGADOR_VS_MAQUINA_1);
        caso.repo.fallarAntes = true;
        caso.victoriaSinDesafio();
        caso.controlador.consultarEstadisticas("Otro usuario");
        caso.drenar();
        comprobar(caso.estado().guardado() == Guardado.ERROR && caso.estado().errorGuardado() != null
                && caso.repo.escrituras == 0, "Fallo de guardado no quedó recuperable");
        caso.vista.salirSinGuardar = false;
        caso.controlador.volverAlMenu();
        comprobar(!caso.estado().menu() && caso.vista.confirmacionesSinGuardar == 1,
                "Ignoró negativa a perder resultado");
        caso.repo.fallarAntes = false;
        caso.controlador.reintentarGuardado();
        caso.controlador.reintentarGuardado();
        comprobar(caso.io.tareas.size() == 1 && caso.estado().guardado() == Guardado.GUARDANDO,
                "Doble reintento duplicó trabajo");
        caso.drenar();
        comprobar(caso.estado().guardado() == Guardado.GUARDADO && caso.repo.escrituras == 1,
                "Reintento no recuperó guardado");
        comprobar(caso.repo.datos.getProperty(caso.estado().resultado().getId().toString()).endsWith("|Ana"),
                "Guardado tomó usuario distinto al de la partida");
        caso.controlador.reintentarGuardado();
        comprobar(caso.io.tareas.isEmpty(), "Reintentó resultado ya guardado");

        // Simula un disco que escribió, pero informó error: el UUID debe hacer idempotente el reintento.
        Escenario respuestaFallida = new Escenario();
        respuestaFallida.iniciarHumano("Ana", ModoJuego.JUGADOR_VS_MAQUINA_1);
        respuestaFallida.repo.fallarDespues = true;
        respuestaFallida.victoriaSinDesafio();
        respuestaFallida.drenar();
        comprobar(respuestaFallida.estado().guardado() == Guardado.ERROR
                && respuestaFallida.repo.escrituras == 1, "No simuló fallo posterior a escritura");
        respuestaFallida.repo.fallarDespues = false;
        respuestaFallida.controlador.reintentarGuardado();
        respuestaFallida.drenar();
        comprobar(respuestaFallida.estado().guardado() == Guardado.GUARDADO
                && respuestaFallida.repo.escrituras == 1
                && respuestaFallida.estado().estadisticasUsuario().getPartidasTotales() == 1,
                "Respuesta fallida produjo doble cómputo");

        Escenario salir = new Escenario();
        salir.iniciarHumano("Ana", ModoJuego.JUGADOR_VS_MAQUINA_1);
        salir.repo.fallarAntes = true;
        salir.victoriaSinDesafio();
        salir.drenar();
        salir.vista.salirSinGuardar = true;
        salir.controlador.solicitarSalir();
        comprobar(salir.vista.cerrada && salir.vista.confirmacionesSinGuardar == 1 && salir.repo.escrituras == 0,
                "Salida sin guardar no respetó confirmación");
    }

    private static void desenlacesHumanos() {
        for (ModoJuego modo : modosHumanos()) {
            Escenario victoria = new Escenario();
            victoria.iniciarHumano("Ana", modo);
            for (int i = 0; i < 15; i++) {
                victoria.fallarJugador();
                victoria.reloj.disparar();
            }
            victoria.controlador.arriesgar(victoria.prueba.primerSecreto.getId());
            comprobar(victoria.estado().estado() == EstadoPartida.FINALIZADA
                    && victoria.estado().resultado().getDesenlace() == Desenlace.VICTORIA,
                    "Victoria con quince descartes ofreció desafío");
            victoria.drenar();
            comprobar(victoria.repo.escrituras == 1, "Victoria directa no se guardó una sola vez");

            Escenario derrota = new Escenario();
            derrota.iniciar("Ana", modo);
            derrota.controlador.seleccionarPersonaje(derrota.prueba.personajes.get(0).getId());
            derrota.fallarJugador();
            derrota.reloj.disparar();
            comprobar(derrota.estado().resultado().getDesenlace() == Desenlace.DERROTA
                    && derrota.estado().secretoRivalFinal().equals(derrota.prueba.primerSecreto),
                    "Derrota inicial no reveló el secreto actual");
            derrota.drenar();
            comprobar(derrota.estado().estadisticasUsuario().getPartidasTotales() == 1
                    && derrota.estado().estadisticasUsuario().getVictorias() == 0,
                    "Derrota inicial contó como victoria");

            Escenario segunda = new Escenario();
            segunda.iniciarHumano("Ana", modo);
            for (int i = 0; i < 14; i++) {
                segunda.fallarJugador();
                segunda.reloj.disparar();
            }
            int heredados = segunda.estado().candidatosDe(segunda.estado().rival()).size();
            segunda.controlador.arriesgar(segunda.prueba.primerSecreto.getId());
            segunda.controlador.decidirSegundaFase(true);
            comprobar(segunda.estado().candidatosDe(segunda.estado().rival()).size() == heredados,
                    "La segunda máquina no recibió los candidatos heredados");
            for (int i = 0; i < 23 && segunda.estado().estado() == EstadoPartida.EN_CURSO; i++) {
                segunda.fallarJugador();
                segunda.reloj.disparar();
            }
            comprobar(segunda.estado().estado() == EstadoPartida.FINALIZADA
                    && segunda.estado().resultado().getDesenlace() == Desenlace.DERROTA,
                    "Perder el desafío no produjo derrota final");
            comprobar(segunda.estado().secretoRivalFinal().equals(segunda.prueba.segundoSecreto),
                    "Derrota del desafío reveló el secreto de la primera fase");
            segunda.drenar();
            comprobar(segunda.repo.escrituras == 1 && segunda.estado().estadisticasUsuario().getVictorias() == 0,
                    "Derrota del desafío guardó también la victoria intermedia");
        }
    }

    private static void estadisticasYRespuestasTardias() throws IOException {
        Escenario caso = new Escenario();
        caso.servicio.registrar("Ana", resultado(Desenlace.VICTORIA));
        caso.servicio.registrar("Beto", resultado(Desenlace.DERROTA));
        caso.servicio.registrar("Beto", resultado(Desenlace.VICTORIA));
        caso.controlador.abrir();
        caso.controlador.consultarEstadisticas("Ana");
        caso.controlador.consultarEstadisticas("Beto");
        caso.io.ejecutarTodas();
        // Entrega la respuesta más nueva primero; las anteriores no deben reemplazarla.
        caso.entregas.removeLast().run();
        comprobar(caso.estado().usuario().equals("Beto")
                && caso.estado().estadisticasUsuario().getPartidasTotales() == 2, "Consulta usó usuario incorrecto");
        int actualizaciones = caso.vista.actualizaciones;
        while (!caso.entregas.isEmpty()) { caso.entregas.removeFirst().run(); }
        comprobar(caso.vista.actualizaciones == actualizaciones, "Respuesta obsoleta refrescó datos de otro usuario");
        caso.repo.fallarLectura = true;
        caso.controlador.consultarEstadisticas("Ana");
        comprobar(caso.estado().cargandoEstadisticas() && caso.estado().estadisticasUsuario() == null,
                "Carga conservó estadísticas del usuario anterior");
        caso.drenar();
        comprobar(!caso.estado().cargandoEstadisticas() && caso.estado().errorEstadisticas() != null
                && caso.estado().estadisticasUsuario() == null && caso.estado().estadisticasMaquinas() == null,
                "Error de lectura se presentó como cero partidas");
        caso.repo.fallarLectura = false;
        caso.controlador.consultarEstadisticas("Ana");
        caso.io.ejecutarTodas();
        caso.controlador.solicitarSalir();
        actualizaciones = caso.vista.actualizaciones;
        caso.drenar();
        caso.controlador.abrir();
        caso.controlador.iniciarPartida("Ana", ModoJuego.JUGADOR_VS_MAQUINA_1);
        comprobar(caso.vista.actualizaciones == actualizaciones && caso.vista.cerrada,
                "Una respuesta o acción tardía reabrió la vista");
    }

    private static void abandonoSinRegistro() {
        Escenario seleccion = new Escenario();
        seleccion.iniciar("Ana", ModoJuego.JUGADOR_VS_MAQUINA_1);
        seleccion.controlador.volverAlMenu();
        seleccion.drenar();
        comprobar(seleccion.estado().menu() && seleccion.repo.escrituras == 0, "Selección abandonada contó partida");
        Escenario decision = new Escenario();
        decision.iniciarHumano("Ana", ModoJuego.JUGADOR_VS_MAQUINA_2);
        decision.controlador.arriesgar(decision.prueba.primerSecreto.getId());
        decision.controlador.volverAlMenu();
        decision.drenar();
        comprobar(decision.estado().menu() && decision.repo.escrituras == 0
                && decision.vista.confirmacionesAbandono == 1, "Decisión incompleta contó victoria");
        Escenario aleatorio = new Escenario();
        aleatorio.iniciar("Ana", ModoJuego.JUGADOR_VS_MAQUINA_1);
        aleatorio.controlador.seleccionarPersonajeAleatorio();
        comprobar(aleatorio.estado().turnoHumano() && aleatorio.estado().secretoJugador() != null,
                "Selección aleatoria no inició partida");
    }

    private static ModoJuego[] modosHumanos() {
        return new ModoJuego[] {ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2};
    }

    private static ResultadoPartida resultado(Desenlace desenlace) {
        return new ResultadoPartida(UUID.randomUUID(), ModoJuego.JUGADOR_VS_MAQUINA_1, desenlace);
    }

    private static final class Escenario {
        final PartidaPrueba prueba = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_1, true);
        final RepositorioMemoria repo = new RepositorioMemoria();
        final ServicioEstadisticas servicio = new ServicioEstadisticas(repo);
        final VistaPrueba vista = new VistaPrueba();
        final RelojPrueba reloj = new RelojPrueba();
        final TrabajoPrueba io = new TrabajoPrueba();
        final Deque<Runnable> entregas = new ArrayDeque<>();
        final ControladorJuego controlador = new ControladorJuego(new Partida(new Random(9),
                new PartidaPrueba.AzarFijo(0, 0), new PartidaPrueba.AzarFijo(0, 0)),
                servicio, vista, reloj, io, entregas::addLast);

        EstadoVistaJuego estado() { return vista.estado; }
        void iniciar(String nombre, ModoJuego modo) {
            controlador.iniciarPartida(nombre, modo);
            drenar();
        }
        void iniciarHumano(String nombre, ModoJuego modo) {
            iniciar(nombre, modo);
            controlador.seleccionarPersonaje(prueba.humano.getId());
        }
        void fallarJugador() {
            Personaje secreto = estado().fase() == 1 ? prueba.primerSecreto : prueba.segundoSecreto;
            int candidato = estado().personajes().stream().filter(p -> !p.equals(secreto)
                    && estado().candidatosDe(Participante.JUGADOR).contains(p.getId()))
                    .findFirst().orElseThrow().getId();
            controlador.arriesgar(candidato);
        }
        void victoriaSinDesafio() {
            controlador.arriesgar(prueba.primerSecreto.getId());
            comprobar(estado().estado() == EstadoPartida.DECISION_SEGUNDA_FASE, "Falta decisión previa");
            controlador.decidirSegundaFase(false);
        }
        void drenar() {
            int limite = 100;
            while (!io.tareas.isEmpty() || !entregas.isEmpty()) {
                comprobar(limite-- > 0, "Se repiten tareas sin completar");
                io.ejecutarTodas();
                while (!entregas.isEmpty()) { entregas.removeFirst().run(); }
            }
        }
    }

    private static final class TrabajoPrueba implements Executor {
        final Deque<Runnable> tareas = new ArrayDeque<>();
        @Override public void execute(Runnable tarea) { tareas.addLast(tarea); }
        void ejecutarTodas() { while (!tareas.isEmpty()) { tareas.removeFirst().run(); } }
    }

    private static final class RelojPrueba implements ControladorJuego.RelojTurnos {
        Runnable pendiente;
        int demora;
        @Override public void programar(int milisegundos, Runnable accion) {
            demora = milisegundos;
            pendiente = accion;
        }
        @Override public void cancelar() { pendiente = null; }
        void disparar() {
            comprobar(pendiente != null, "No hay turno programado");
            Runnable accion = pendiente;
            pendiente = null;
            accion.run();
        }
    }

    private static final class VistaPrueba implements IVistaJuego {
        EstadoVistaJuego estado;
        final List<String> errores = new ArrayList<>();
        int actualizaciones;
        int confirmacionesAbandono;
        int confirmacionesSinGuardar;
        boolean abandonar = true;
        boolean salirSinGuardar = true;
        boolean cerrada;
        Runnable duranteConfirmacion;
        @Override public void actualizar(EstadoVistaJuego nuevoEstado) { estado = nuevoEstado; actualizaciones++; }
        @Override public void mostrarError(String mensaje) { errores.add(mensaje); }
        @Override public boolean confirmarAbandono() {
            confirmacionesAbandono++;
            if (duranteConfirmacion != null) { duranteConfirmacion.run(); }
            return abandonar;
        }
        @Override public boolean confirmarSalidaSinGuardar() { confirmacionesSinGuardar++; return salirSinGuardar; }
        @Override public void cerrar() { cerrada = true; }
    }

    private static final class RepositorioMemoria implements IRepositorioEstadisticas {
        Properties datos = new Properties();
        int escrituras;
        boolean fallarLectura;
        boolean fallarAntes;
        boolean fallarDespues;
        @Override public Properties cargar() throws IOException {
            if (fallarLectura) { throw new IOException("No se pudo leer el registro"); }
            return copia(datos);
        }
        @Override public void guardar(Properties registro) throws IOException {
            if (fallarAntes) { throw new IOException("No se pudo escribir el registro"); }
            datos = copia(registro);
            escrituras++;
            if (fallarDespues) { throw new IOException("No se pudo confirmar la escritura"); }
        }
        private static Properties copia(Properties original) {
            Properties copia = new Properties();
            copia.putAll(original);
            return copia;
        }
    }
}
