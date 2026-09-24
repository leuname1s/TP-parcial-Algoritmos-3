package Interfaces;

import datos.ResultadoTurno;

public interface IMaquina {
    String getNombre();
    ResultadoTurno ejecutarTurno(IArbitroTurno arbitro);
    ITableroCandidatos getTablero();
}
