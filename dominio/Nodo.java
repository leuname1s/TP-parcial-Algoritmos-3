package adivinanzas.dominio;

class Nodo {
    final Personaje dato;
    Nodo siguiente;

    Nodo(Personaje dato){
        this.dato = dato;
        this.siguiente = null;

    }
}
