package datos;

import Interfaces.IMazoPersonajes;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MazoPersonajes implements IMazoPersonajes {

    public static final int TOTAL = 23;
    private static final int COMBINACIONES = 24;

    private Nodo cabeza;
    private final Personaje[] indicePorId = new Personaje[TOTAL + 1];
    private final Personaje[] duenoDeCombinacion = new Personaje[COMBINACIONES];

    private int cantidad = 0;

    @Override
    public Personaje agregar(String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo) {
        if (cantidad == TOTAL) {
            throw new IllegalStateException("El mazo ya tiene " + TOTAL + " personajes");
        }
        int id = cantidad + 1;
        int codigo = codigoDe(genero, calvo, lentes, colorPelo);
        if (duenoDeCombinacion[codigo] != null) {
            throw new IllegalArgumentException(
                    "Combinacion repetida: " + nombre + " es identico a "
                            + duenoDeCombinacion[codigo].getNombre());
        }
        Personaje p = new Personaje(id, nombre, genero, colorPelo, lentes, calvo);
        insertarOrdenado(p);
        indicePorId[id] = p;
        cantidad++;
        duenoDeCombinacion[codigo] = p;
        return p;
    }

    private static int codigoDe(Genero genero, boolean calvo,
                                boolean lentes, ColorPelo colorPelo) {
        int codigo = genero.getOrden();
        codigo = codigo * ColorPelo.values().length + colorPelo.ordinal();
        codigo = codigo * 2 + (calvo ? 1 : 0);
        codigo = codigo * 2 + (lentes ? 1 : 0);
        return codigo;
    }

    private void insertarOrdenado(Personaje p) {
        Nodo nuevo = new Nodo(p);
        int ordenNuevo = p.getGenero().getOrden();
        if (cabeza == null || ordenNuevo < cabeza.dato.getGenero().getOrden()) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
            return;
        }
        Nodo actual = cabeza;
        while (actual.siguiente != null && actual.siguiente.dato.getGenero().getOrden() <= ordenNuevo) {
            actual = actual.siguiente;
        }
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
    }

    @Override
    public Personaje buscarPorId(int id) {
        if (id < 1 || id > TOTAL) {
            return null;
        }
        return indicePorId[id];
    }

    @Override
    public int getCantidad() {
        return cantidad;
    }

    @Override
    public boolean estaCompleto() {
        return cantidad == TOTAL;
    }

    @Override
    public Iterator<Personaje> iterator() {
        return new Iterator<Personaje>() {
            private Nodo actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public Personaje next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                Personaje p = actual.dato;
                actual = actual.siguiente;
                return p;
            }
        };
    }
}
