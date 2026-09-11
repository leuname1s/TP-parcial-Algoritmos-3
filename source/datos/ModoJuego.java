package datos;

public enum ModoJuego {
    JUGADOR_VS_MAQUINA_1(1, "Jugador vs Máquina 1"),
    JUGADOR_VS_MAQUINA_2(2, "Jugador vs Máquina 2"),
    MAQUINA_1_VS_MAQUINA_2(3, "Máquina 1 vs Máquina 2"),
    SALIR(0, "Salir");

    private final int opcion;
    private final String descripcion;

    ModoJuego(int opcion, String descripcion) {
        this.opcion = opcion;
        this.descripcion = descripcion;
    }

    public int getOpcion() {
        return opcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static ModoJuego obtenerPorOpcion(int opcion) {
        for (ModoJuego modo : values()) {
            if (modo.opcion == opcion) {
                return modo;
            }
        }
        return null;
    }
}
