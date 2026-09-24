package datos;

import java.util.Objects;

/** Una sola acción válida, distinta del resultado definitivo de la partida. */
public final class ResultadoTurno {
    private final String participante;
    private final Pregunta pregunta;
    private final Boolean respuesta;
    private final Personaje intento;
    private final boolean acierto;
    private final int descartados;
    private final int candidatosRestantes;
    private final DiagnosticoTurno diagnostico;

    private ResultadoTurno(String participante, Pregunta pregunta, Boolean respuesta,
            Personaje intento, boolean acierto, int descartados, int candidatosRestantes,
            DiagnosticoTurno diagnostico) {
        this.participante = Objects.requireNonNull(participante);
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.intento = intento;
        this.acierto = acierto;
        this.descartados = descartados;
        this.candidatosRestantes = candidatosRestantes;
        this.diagnostico = diagnostico;
    }

    public static ResultadoTurno pregunta(String participante, Pregunta pregunta, boolean respuesta,
            int descartados, int restantes, DiagnosticoTurno diagnostico) {
        return new ResultadoTurno(participante, Objects.requireNonNull(pregunta), respuesta,
                null, false, descartados, restantes, diagnostico);
    }

    public static ResultadoTurno intento(String participante, Personaje intento, boolean acierto,
            int restantes, DiagnosticoTurno diagnostico) {
        return new ResultadoTurno(participante, null, null, Objects.requireNonNull(intento),
                acierto, acierto ? 0 : 1, restantes, diagnostico);
    }

    public String getParticipante() { return participante; }
    /** null cuando la acción fue un intento; en ese caso también lo es la respuesta. */
    public Pregunta getPregunta() { return pregunta; }
    public Boolean getRespuesta() { return respuesta; }
    /** Solo contiene el personaje intentado; una pregunta no revela el secreto. */
    public Personaje getIntento() { return intento; }
    public boolean isAcierto() { return acierto; }
    public int getDescartados() { return descartados; }
    public int getCandidatosRestantes() { return candidatosRestantes; }
    /** null para las acciones humanas, que no ejecutan una estrategia de máquina. */
    public DiagnosticoTurno getDiagnostico() { return diagnostico; }
}
