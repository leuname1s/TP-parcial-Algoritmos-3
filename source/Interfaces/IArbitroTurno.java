package Interfaces;

import datos.Pregunta;

public interface IArbitroTurno {
    boolean responder(Pregunta pregunta);
    boolean comprobarIntento(int id);
}
