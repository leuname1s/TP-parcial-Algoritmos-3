package Interfaces;

import datos.Personaje;

public interface IMazoPersonajes extends Iterable<Personaje> {
    void agregar(Personaje personaje);
    int getMaxId();
    Personaje buscarPorId(int id);
    int getCantidad();
    boolean estaCompleto();
}
