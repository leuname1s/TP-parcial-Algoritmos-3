package Interfaces;

import datos.ColorPelo;
import datos.Genero;

public interface IPersonaje {
    int getId();
    String getNombre();
    Genero getGenero();
    ColorPelo getColorPelo();
    boolean isTieneLentes();
    boolean isTieneBarba();
    boolean isLeFaltaUnDiente();
}
