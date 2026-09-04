package interfaces;

import datos.Personaje;

public interface IMaquina {
    String getNombre();
    boolean ejecutarTurno(Personaje objetivoEnemigo);
    ITableroCandidatos getTablero();
}
