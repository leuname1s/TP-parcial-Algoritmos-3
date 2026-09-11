package Interfaces;

import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoPartida;
import datos.ResultadoTurno;
import java.util.List;
import java.util.Set;

/**
 * Motor por acciones, sin entrada/salida. Rechaza datos inválidos con IllegalArgumentException
 * y acciones fuera de estado o turno con IllegalStateException, sin consumir el turno.
 */
public interface IPartida {
    void iniciar(ModoJuego modo);
    void seleccionarPersonaje(int id);
    void seleccionarPersonajeAleatorio();
    ResultadoTurno preguntar(Pregunta pregunta);
    ResultadoTurno arriesgar(int id);
    ResultadoTurno ejecutarTurnoMaquina();
    void decidirSegundaFase(boolean aceptar);
    EstadoPartida getEstado();
    ModoJuego getModo();
    /** null durante la selección, la decisión de segunda fase y después del final. */
    Participante getTurno();
    Participante getRivalActual();
    int getFase();
    int getRonda();
    ResultadoPartida getResultado();
    Personaje getPersonajeJugador();
    /** Solo disponible en modo espectador; rechaza consultas en modos humanos. */
    Personaje getSecretoEspectador(Participante participante);
    /** Las colecciones consultadas son copias inmutables del momento de la consulta. */
    List<Personaje> getPersonajes();
    List<Personaje> getCandidatos(Participante participante);
    Set<Pregunta> getPreguntasRealizadas();
}
