package Interfaces;

public interface IMaquina {
    String getNombre();
    boolean ejecutarTurno(IArbitroTurno arbitro);
    ITableroCandidatos getTablero();
}
