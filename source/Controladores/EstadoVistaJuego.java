package Controladores;

import datos.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Instantánea de presentación; no entrega a la vista el motor ni tableros modificables. */
public record EstadoVistaJuego(
        boolean menu, String usuario, EstadoPartida estado, ModoJuego modo,
        Participante turno, Participante rival, int fase, int ronda,
        List<Personaje> personajes, Map<Participante, Set<Integer>> candidatos,
        Personaje secretoJugador, Map<Participante, Personaje> secretosEspectador,
        Personaje secretoRivalFinal, Set<Pregunta> preguntasRealizadas,
        List<TurnoRegistrado> historial, ResultadoPartida resultado,
        boolean pausado, int demoraMaquina, Guardado guardado,
        Estadisticas estadisticasUsuario, Estadisticas estadisticasMaquinas,
        boolean cargandoEstadisticas, String errorEstadisticas, String errorGuardado) {

    public enum Guardado { NO_CORRESPONDE, GUARDANDO, GUARDADO, ERROR }

    public EstadoVistaJuego {
        personajes = List.copyOf(personajes);
        candidatos = candidatos.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entrada -> Set.copyOf(entrada.getValue())));
        secretosEspectador = Map.copyOf(secretosEspectador);
        preguntasRealizadas = Set.copyOf(preguntasRealizadas);
        historial = List.copyOf(historial);
    }

    public boolean espectador() { return modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2; }
    public boolean turnoHumano() { return estado == EstadoPartida.EN_CURSO && turno == Participante.JUGADOR; }
    public Set<Integer> candidatosDe(Participante participante) { return candidatos.getOrDefault(participante, Set.of()); }
}
