package datos;

import java.util.Objects;
import java.util.UUID;

/** Resultado definitivo; las fases intermedias no se contabilizan. */
public final class ResultadoPartida {
    public enum Desenlace { DERROTA, VICTORIA, VICTORIA_VERDADERA, GANA_MAQUINA_1, GANA_MAQUINA_2 }

    private final UUID id;
    private final ModoJuego modo;
    private final Desenlace desenlace;

    public ResultadoPartida(UUID id, ModoJuego modo, Desenlace desenlace) {
        this.id = Objects.requireNonNull(id);
        this.modo = Objects.requireNonNull(modo);
        this.desenlace = Objects.requireNonNull(desenlace);
        boolean maquinas = desenlace == Desenlace.GANA_MAQUINA_1 || desenlace == Desenlace.GANA_MAQUINA_2;
        if (modo == ModoJuego.SALIR || maquinas != (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2)) {
            throw new IllegalArgumentException("Resultado incompatible con el modo");
        }
    }

    public UUID getId() { return id; }
    public ModoJuego getModo() { return modo; }
    public Desenlace getDesenlace() { return desenlace; }
}
