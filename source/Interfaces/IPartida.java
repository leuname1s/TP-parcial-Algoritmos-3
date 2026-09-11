package Interfaces;

import datos.ModoJuego;
import datos.Personaje;

public interface IPartida {
    void iniciar(ModoJuego modo);
    void jugar();
    datos.ResultadoPartida getResultado();
    Personaje getPersonajeJugador();
    Personaje getObjetivoMaquina1();
    Personaje getObjetivoMaquina2();
    ITableroCandidatos getTableroJugador();
    ITableroCandidatos getTableroMaquina1();
    ITableroCandidatos getTableroMaquina2();
}
