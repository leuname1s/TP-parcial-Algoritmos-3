package Controladores;

import datos.ResultadoTurno;
import java.util.Objects;

/** Conserva la ubicación del turno anterior a cualquier transición del motor. */
public record TurnoRegistrado(int fase, int ronda, ResultadoTurno resultado) {
    public TurnoRegistrado {
        Objects.requireNonNull(resultado);
    }
}
