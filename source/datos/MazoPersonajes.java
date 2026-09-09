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
    private Nodo cola;
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
        Nodo nuevo = new Nodo(p);
        if (cola == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        indicePorId[p.getId()] = p;
        cantidad++;
        duenoDeCombinacion[codigo] = p;
    }

    // Cada atributo ocupa una posición según su cantidad de valores posibles;
    // así, cada perfil tiene un índice único para detectar combinaciones repetidas.
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

    /**
     * Ordena por genero con MergeSort estable, sin cambiar personajes ni IDs.
     * Reenlaza nodos en O(n log n), con O(log n) de pila recursiva.
     * Debe ejecutarse al terminar la carga y antes de iniciar un recorrido.
     */
    @Override
    public void ordenarPorGenero() {
        cabeza = mergeSort(cabeza);
        cola = cabeza;
        while (cola != null && cola.siguiente != null) {
            cola = cola.siguiente;
        }
    }

    private static Nodo mergeSort(Nodo inicio) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        // La referencia rápida avanza de a dos: cuando termina, la lenta marca el corte.
        Nodo lento = inicio;
        Nodo rapido = inicio.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        Nodo derecha = lento.siguiente;
        lento.siguiente = null;
        return mezclar(mergeSort(inicio), mergeSort(derecha));
    }

    private static Nodo mezclar(Nodo izquierda, Nodo derecha) {
        Nodo inicio = null;
        Nodo ultimo = null;
        while (izquierda != null && derecha != null) {
            Nodo elegido;
            // Elegir la izquierda en empates conserva el orden de carga.
            if (izquierda.dato.getGenero().getOrden() <= derecha.dato.getGenero().getOrden()) {
                elegido = izquierda;
                izquierda = izquierda.siguiente;
            } else {
                elegido = derecha;
                derecha = derecha.siguiente;
            }
            if (ultimo == null) {
                inicio = elegido;
            } else {
                ultimo.siguiente = elegido;
            }
            ultimo = elegido;
        }
        // La mitad que queda ya está ordenada y se enlaza completa.
        Nodo resto = izquierda != null ? izquierda : derecha;
        if (ultimo == null) {
            return resto;
        }
        ultimo.siguiente = resto;
        return inicio;
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
