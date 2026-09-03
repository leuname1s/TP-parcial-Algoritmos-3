package interfaces;

import datos.ColorPelo;
import datos.Genero;
import datos.Personaje;

public interface IMazoPersonajes extends Iterable<Personaje> {
    Personaje agregar(String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo);
    Personaje buscarPorId(int id);
    int getCantidad();
    boolean estaCompleto();
}
