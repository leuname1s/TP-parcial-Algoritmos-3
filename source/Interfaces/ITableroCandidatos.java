package Interfaces;

import datos.Personaje;
import datos.Pregunta;

public interface ITableroCandidatos {
    boolean estaVivo(int id);
    int getCantidadViva();
    int descartarSegun(Pregunta pregunta, boolean respuesta);
    void descartar(int id);
    Personaje unicoSobreviviente();
    int contarSiCumplen(Pregunta pregunta);
}
