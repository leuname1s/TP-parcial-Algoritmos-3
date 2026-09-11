package Funcionalidades;

import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import Interfaces.IPartida;
import Interfaces.ITableroCandidatos;
import datos.EstadoPartida;
import datos.MazoPersonajes;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoPartida;
import datos.ResultadoPartida.Desenlace;
import datos.ResultadoTurno;
import defaults.CatalogoPersonajes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Reglas y estado de la partida, independientes de consola, ventanas y persistencia. */
public class Partida implements IPartida {
    private final Random random;
    private final Random randomMaquina1;
    private final Random randomMaquina2;
    private UUID idPartida;
    private ResultadoPartida resultado;
    private EstadoPartida estado = EstadoPartida.SIN_INICIAR;
    private ModoJuego modoActual;
    private Participante turno;
    private Participante rivalActual;
    private int fase;
    private int ronda;
    private MazoPersonajes mazo;
    private Personaje personajeJugador;
    private Personaje secretoMaquina1;
    private Personaje secretoMaquina2;
    private TableroCandidatos tableroJugador;
    private IMaquina maquina1;
    private IMaquina maquina2;
    private final EnumSet<Pregunta> preguntasJugador = EnumSet.noneOf(Pregunta.class);

    public Partida() { this(new Random()); }

    public Partida(Random random) { this(random, new Random(), new Random()); }

    /** Fuentes independientes para reproducir sorteos sin acoplar secretos y estrategias. */
    public Partida(Random random, Random randomMaquina1, Random randomMaquina2) {
        this.random = Objects.requireNonNull(random);
        this.randomMaquina1 = Objects.requireNonNull(randomMaquina1);
        this.randomMaquina2 = Objects.requireNonNull(randomMaquina2);
    }

    @Override
    public void iniciar(ModoJuego modo) {
        if (modo == null || modo == ModoJuego.SALIR) {
            throw new IllegalArgumentException("Modo de partida inválido");
        }
        mazo = CatalogoPersonajes.crearMazo(random);
        idPartida = UUID.randomUUID();
        resultado = null;
        modoActual = modo;
        personajeJugador = null;
        secretoMaquina1 = null;
        secretoMaquina2 = null;
        tableroJugador = null;
        maquina1 = null;
        maquina2 = null;
        preguntasJugador.clear();
        fase = 1;
        ronda = 1;
        turno = null;
        rivalActual = null;

        if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            secretoMaquina1 = sortearPersonaje(null);
            secretoMaquina2 = sortearPersonaje(secretoMaquina1);
            maquina1 = crearMaquina(Participante.MAQUINA_1, new TableroCandidatos(mazo));
            maquina2 = crearMaquina(Participante.MAQUINA_2, new TableroCandidatos(mazo));
            turno = Participante.MAQUINA_1;
            estado = EstadoPartida.EN_CURSO;
        } else {
            rivalActual = modo == ModoJuego.JUGADOR_VS_MAQUINA_1
                    ? Participante.MAQUINA_1 : Participante.MAQUINA_2;
            estado = EstadoPartida.SELECCION_PERSONAJE;
        }
    }

    @Override
    public void seleccionarPersonaje(int id) {
        exigirEstado(EstadoPartida.SELECCION_PERSONAJE);
        Personaje elegido = mazo.buscarPorId(id);
        if (elegido == null) {
            throw new IllegalArgumentException("Ese ID no pertenece a esta partida");
        }
        prepararJugador(elegido);
    }

    @Override
    public void seleccionarPersonajeAleatorio() {
        exigirEstado(EstadoPartida.SELECCION_PERSONAJE);
        prepararJugador(sortearPersonaje(null));
    }

    private void prepararJugador(Personaje elegido) {
        personajeJugador = elegido;
        tableroJugador = new TableroCandidatos(mazo);
        Personaje secreto = sortearPersonaje(null);
        IMaquina maquina = crearMaquina(rivalActual, new TableroCandidatos(mazo));
        if (rivalActual == Participante.MAQUINA_1) {
            secretoMaquina1 = secreto;
            maquina1 = maquina;
        } else {
            secretoMaquina2 = secreto;
            maquina2 = maquina;
        }
        turno = Participante.JUGADOR;
        estado = EstadoPartida.EN_CURSO;
    }

    @Override
    public ResultadoTurno preguntar(Pregunta pregunta) {
        exigirTurnoJugador();
        // Validar antes de tocar candidatos, historial o turno mantiene atómico el rechazo.
        if (pregunta == null) { throw new IllegalArgumentException("Falta la pregunta"); }
        if (preguntasJugador.contains(pregunta)) {
            throw new IllegalArgumentException("Ya realizaste esa pregunta; no se consume el turno");
        }
        boolean respuesta = pregunta.cumple(secretoDe(rivalActual));
        int descartados = tableroJugador.descartarSegun(pregunta, respuesta);
        preguntasJugador.add(pregunta);
        turno = rivalActual;
        return ResultadoTurno.pregunta(Participante.JUGADOR.getNombre(), pregunta, respuesta,
                descartados, tableroJugador.getCantidadViva(), null);
    }

    @Override
    public ResultadoTurno arriesgar(int id) {
        exigirTurnoJugador();
        Personaje candidato = mazo.buscarPorId(id);
        if (candidato == null) {
            throw new IllegalArgumentException("Ese ID no pertenece a esta partida");
        }
        if (!tableroJugador.estaVivo(id)) {
            throw new IllegalArgumentException("Ese personaje ya fue descartado");
        }
        boolean acierto = candidato.equals(secretoDe(rivalActual));
        if (acierto) {
            resolverVictoriaJugador();
        } else {
            tableroJugador.descartar(id);
            turno = rivalActual;
        }
        return ResultadoTurno.intento(Participante.JUGADOR.getNombre(), candidato, acierto,
                tableroJugador.getCantidadViva(), null);
    }

    private void resolverVictoriaJugador() {
        if (fase == 2) {
            finalizar(Desenlace.VICTORIA_VERDADERA);
        } else if (mazo.getCantidad() - maquinaDe(rivalActual).getTablero().getCantidadViva() < 15) {
            // La victoria de fase no es todavía un resultado registrable: falta la decisión.
            estado = EstadoPartida.DECISION_SEGUNDA_FASE;
            turno = null;
        } else {
            finalizar(Desenlace.VICTORIA);
        }
    }

    @Override
    public ResultadoTurno ejecutarTurnoMaquina() {
        exigirEstado(EstadoPartida.EN_CURSO);
        if (turno == Participante.JUGADOR) {
            throw new IllegalStateException("Es el turno del jugador");
        }
        Participante actuante = turno;
        Personaje objetivo = modoActual == ModoJuego.MAQUINA_1_VS_MAQUINA_2
                ? secretoDe(otraMaquina(actuante)) : personajeJugador;
        ResultadoTurno accion = maquinaDe(actuante).ejecutarTurno(crearArbitro(objetivo));
        if (accion.isAcierto()) {
            if (modoActual == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
                finalizar(actuante == Participante.MAQUINA_1
                        ? Desenlace.GANA_MAQUINA_1 : Desenlace.GANA_MAQUINA_2);
            } else {
                finalizar(Desenlace.DERROTA);
            }
        } else if (modoActual == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            turno = otraMaquina(actuante);
            if (turno == Participante.MAQUINA_1) { ronda++; }
        } else {
            turno = Participante.JUGADOR;
            ronda++;
        }
        return accion;
    }

    @Override
    public void decidirSegundaFase(boolean aceptar) {
        exigirEstado(EstadoPartida.DECISION_SEGUNDA_FASE);
        if (!aceptar) {
            finalizar(Desenlace.VICTORIA);
            return;
        }
        Personaje secretoAnterior = secretoDe(rivalActual);
        TableroCandidatos heredado = (TableroCandidatos) maquinaDe(rivalActual).getTablero();
        Participante siguiente = otraMaquina(rivalActual);
        Personaje nuevoSecreto = sortearPersonaje(secretoAnterior);
        // Cada máquina copia el tablero recibido: la segunda no puede alterar a la primera.
        IMaquina nuevaMaquina = crearMaquina(siguiente, heredado);
        if (siguiente == Participante.MAQUINA_1) {
            secretoMaquina1 = nuevoSecreto;
            maquina1 = nuevaMaquina;
        } else {
            secretoMaquina2 = nuevoSecreto;
            maquina2 = nuevaMaquina;
        }
        // El humano conserva su secreto, pero busca uno nuevo entre los mismos 23 menos el anterior.
        tableroJugador = new TableroCandidatos(mazo);
        tableroJugador.descartar(secretoAnterior.getId());
        preguntasJugador.clear();
        rivalActual = siguiente;
        fase = 2;
        ronda = 1;
        turno = Participante.JUGADOR;
        estado = EstadoPartida.EN_CURSO;
    }

    private IArbitroTurno crearArbitro(final Personaje objetivo) {
        return new IArbitroTurno() {
            @Override public boolean responder(Pregunta pregunta) { return pregunta.cumple(objetivo); }
            @Override public boolean comprobarIntento(int id) { return id == objetivo.getId(); }
        };
    }

    private IMaquina crearMaquina(Participante participante, TableroCandidatos tablero) {
        return participante == Participante.MAQUINA_1
                ? new Maquina1(mazo, tablero, randomMaquina1)
                : new Maquina2(mazo, tablero, randomMaquina2);
    }

    private Personaje sortearPersonaje(Personaje excluido) {
        List<Personaje> disponibles = new ArrayList<>();
        for (Personaje personaje : mazo) {
            if (!personaje.equals(excluido)) { disponibles.add(personaje); }
        }
        return disponibles.get(random.nextInt(disponibles.size()));
    }

    private Participante otraMaquina(Participante participante) {
        return participante == Participante.MAQUINA_1 ? Participante.MAQUINA_2 : Participante.MAQUINA_1;
    }

    private IMaquina maquinaDe(Participante participante) {
        return participante == Participante.MAQUINA_1 ? maquina1 : maquina2;
    }

    private Personaje secretoDe(Participante participante) {
        return participante == Participante.MAQUINA_1 ? secretoMaquina1 : secretoMaquina2;
    }

    private void finalizar(Desenlace desenlace) {
        resultado = new ResultadoPartida(idPartida, modoActual, desenlace);
        estado = EstadoPartida.FINALIZADA;
        turno = null;
    }

    private void exigirEstado(EstadoPartida esperado) {
        if (estado != esperado) {
            throw new IllegalStateException("Acción no disponible en el estado " + estado);
        }
    }

    private void exigirTurnoJugador() {
        exigirEstado(EstadoPartida.EN_CURSO);
        if (turno != Participante.JUGADOR) {
            throw new IllegalStateException("No es el turno del jugador");
        }
    }

    @Override public EstadoPartida getEstado() { return estado; }
    @Override public ModoJuego getModo() { return modoActual; }
    @Override public Participante getTurno() { return turno; }
    @Override public Participante getRivalActual() { return rivalActual; }
    @Override public int getFase() { return fase; }
    @Override public int getRonda() { return ronda; }
    @Override public ResultadoPartida getResultado() { return resultado; }
    @Override public Personaje getPersonajeJugador() { return personajeJugador; }

    @Override
    public Personaje getSecretoEspectador(Participante participante) {
        if (modoActual != ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            throw new IllegalStateException("Los secretos rivales no están disponibles en modos humanos");
        }
        if (participante == null || participante == Participante.JUGADOR) {
            throw new IllegalArgumentException("Debe seleccionar una máquina");
        }
        return secretoDe(participante);
    }

    @Override
    public List<Personaje> getPersonajes() {
        if (mazo == null) { return List.of(); }
        List<Personaje> personajes = new ArrayList<>();
        for (Personaje personaje : mazo) { personajes.add(personaje); }
        return List.copyOf(personajes);
    }

    @Override
    public List<Personaje> getCandidatos(Participante participante) {
        if (participante == null) { throw new IllegalArgumentException("Falta el participante"); }
        ITableroCandidatos tablero = tableroJugador;
        if (participante != Participante.JUGADOR) {
            IMaquina maquina = maquinaDe(participante);
            tablero = maquina == null ? null : maquina.getTablero();
        }
        if (tablero == null) { return List.of(); }
        List<Personaje> candidatos = new ArrayList<>();
        for (Personaje personaje : mazo) {
            if (tablero.estaVivo(personaje.getId())) { candidatos.add(personaje); }
        }
        // Una copia inmutable impide que la vista descarte candidatos fuera de las reglas.
        return List.copyOf(candidatos);
    }

    @Override
    public Set<Pregunta> getPreguntasRealizadas() {
        return Collections.unmodifiableSet(EnumSet.copyOf(preguntasJugador));
    }
}
