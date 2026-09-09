package Interfaces;

import datos.Personaje;

public interface IMazoPersonajes extends Iterable<Personaje> {
    /** Agrega al final; no ordena la coleccion. */
    void agregar(Personaje personaje);
    /** Ordena por genero de forma estable al finalizar la carga. */
    void ordenarPorGenero();
    int getMaxId();
    Personaje buscarPorId(int id);
    int getCantidad();
    boolean estaCompleto();
}
