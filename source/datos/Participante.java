package datos;

public enum Participante {
    JUGADOR("Jugador"), MAQUINA_1("Máquina 1"), MAQUINA_2("Máquina 2");

    private final String nombre;
    Participante(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
}
