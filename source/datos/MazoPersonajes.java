package datos;

import Interfaces.IMazoPersonajes;

import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MazoPersonajes implements IMazoPersonajes {

    public static final int TOTAL = 23;
    private static final int COMBINACIONES = Genero.values().length * ColorPelo.values().length * 8;

    private Nodo cabeza;
    private Personaje[] indicePorId = new Personaje[1];
    private final int capacidad;
    private final Personaje[] duenoDeCombinacion = new Personaje[COMBINACIONES];

    private int cantidad = 0;

    public MazoPersonajes() {
        this(TOTAL);
    }

    public MazoPersonajes(int capacidad) {
        if (capacidad < 1) {
            throw new IllegalArgumentException("La capacidad debe ser positiva");
        }
        this.capacidad = capacidad;
    }

    @Override
    public void agregar(Personaje p) {
        Objects.requireNonNull(p);
        if (cantidad == capacidad) {
            throw new IllegalStateException("El mazo ya tiene " + capacidad + " personajes");
        }
        if (buscarPorId(p.getId()) != null) {
            throw new IllegalArgumentException("ID repetido: " + p.getId());
        }
        int codigo = codigoDe(p);
        if (duenoDeCombinacion[codigo] != null) {
            throw new IllegalArgumentException(
                    "Combinacion repetida: " + p.getNombre() + " es identico a "
                            + duenoDeCombinacion[codigo].getNombre());
        }
        if (p.getId() >= indicePorId.length) {
            indicePorId = Arrays.copyOf(indicePorId, p.getId() + 1);
        }
        insertarOrdenado(p);
        indicePorId[p.getId()] = p;
        cantidad++;
        duenoDeCombinacion[codigo] = p;
    }

    private static int codigoDe(Personaje p) {
        int codigo = p.getGenero().getOrden();
        codigo = codigo * ColorPelo.values().length + p.getColorPelo().ordinal();
        codigo = codigo * 2 + (p.isTieneLentes() ? 1 : 0);
        codigo = codigo * 2 + (p.isTieneBarba() ? 1 : 0);
        codigo = codigo * 2 + (p.isLeFaltaUnDiente() ? 1 : 0);
        return codigo;
    }

    @Override
    public int getMaxId() {
        return indicePorId.length - 1;
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
        if (id < 1 || id >= indicePorId.length) {
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
        return cantidad == capacidad;
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
