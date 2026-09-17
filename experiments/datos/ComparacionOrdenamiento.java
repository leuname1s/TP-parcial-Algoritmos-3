package datos;

import defaults.CatalogoPersonajes;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Comparación reproducible del ordenamiento; no interviene en las partidas. */
public final class ComparacionOrdenamiento {
    private static final int SEMILLAS = 100;
    private static final int REPETICIONES = 100;
    private static final int CALENTAMIENTO = 15;
    private static final int TANDAS = 30;
    private static final Comparator<Personaje> GENERO =
            Comparator.comparingInt(p -> p.getGenero().getOrden());

    private ComparacionOrdenamiento() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Indicar la ruta del CSV de salida");
        }
        Path salida = Path.of(args[0]);
        if (salida.toAbsolutePath().getParent() != null) {
            Files.createDirectories(salida.toAbsolutePath().getParent());
        }
        verificarCasosLimite();
        System.out.println("Java: " + System.getProperty("java.runtime.version")
                + "; VM: " + System.getProperty("java.vm.name"));
        System.out.println("SO: " + System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        System.out.println("Procesadores logicos: " + Runtime.getRuntime().availableProcessors());
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(salida, StandardCharsets.UTF_8))) {
            csv.println("escenario,tanda,primero,algoritmo,ordenamientos,total_ns,ms_por_ordenamiento");
            for (String escenario : List.of("mezclado", "ordenado", "invertido")) {
                List<List<Personaje>> entradas = entradas(escenario);
                double[] merge = new double[TANDAS];
                double[] insercion = new double[TANDAS];
                for (int tanda = -CALENTAMIENTO; tanda < TANDAS; tanda++) {
                    MazoPersonajes[] mazos = new MazoPersonajes[SEMILLAS * REPETICIONES];
                    ListaInsercion[] listas = new ListaInsercion[mazos.length];
                    // Construcción, índices y asignación de nodos quedan fuera del cronómetro.
                    for (int i = 0; i < mazos.length; i++) {
                        List<Personaje> entrada = entradas.get(i % SEMILLAS);
                        mazos[i] = crearMazo(entrada);
                        listas[i] = new ListaInsercion(entrada);
                    }
                    boolean mergePrimero = tanda % 2 == 0;
                    long tiempoMerge;
                    long tiempoInsercion;
                    if (mergePrimero) {
                        tiempoMerge = medirMerge(mazos);
                        tiempoInsercion = medirInsercion(listas);
                    } else {
                        tiempoInsercion = medirInsercion(listas);
                        tiempoMerge = medirMerge(mazos);
                    }
                    // Se consume y verifica cada resultado, también durante el calentamiento.
                    for (int i = 0; i < mazos.length; i++) {
                        verificar(mazos[i], listas[i], entradas.get(i % SEMILLAS));
                    }
                    if (tanda >= 0) {
                        merge[tanda] = tiempoMerge / (mazos.length * 1_000_000.0);
                        insercion[tanda] = tiempoInsercion / (mazos.length * 1_000_000.0);
                        String primero = mergePrimero ? "MergeSort" : "Insercion";
                        fila(csv, escenario, tanda, primero, "MergeSort", mazos.length, tiempoMerge);
                        fila(csv, escenario, tanda, primero, "Insercion", mazos.length, tiempoInsercion);
                    }
                }
                resumen(escenario, "MergeSort", merge);
                resumen(escenario, "Insercion", insercion);
            }
            if (csv.checkError()) {
                throw new IllegalStateException("No se pudo escribir el CSV completo");
            }
        }
        System.out.println("PASS: orden estable, identidad, ID, cantidad e indices en todos los resultados");
    }

    private static List<List<Personaje>> entradas(String escenario) {
        List<Personaje> catalogo = new ArrayList<>();
        CatalogoPersonajes.crearCatalogo().forEach(catalogo::add);
        List<List<Personaje>> entradas = new ArrayList<>();
        for (int semilla = 0; semilla < SEMILLAS; semilla++) {
            List<Personaje> mezcla = new ArrayList<>(catalogo);
            Collections.shuffle(mezcla, new Random(semilla));
            List<Personaje> entrada = new ArrayList<>(mezcla.subList(0, 23));
            if (escenario.equals("ordenado")) {
                entrada.sort(GENERO);
            } else if (escenario.equals("invertido")) {
                entrada.sort(GENERO.reversed());
            }
            entradas.add(entrada);
        }
        return entradas;
    }

    private static MazoPersonajes crearMazo(List<Personaje> entrada) {
        MazoPersonajes mazo = new MazoPersonajes(Math.max(1, entrada.size()));
        entrada.forEach(mazo::agregar);
        return mazo;
    }

    private static long medirMerge(MazoPersonajes[] mazos) {
        long inicio = System.nanoTime();
        for (MazoPersonajes mazo : mazos) {
            mazo.ordenarPorGenero();
        }
        return System.nanoTime() - inicio;
    }

    private static long medirInsercion(ListaInsercion[] listas) {
        long inicio = System.nanoTime();
        for (ListaInsercion lista : listas) {
            lista.ordenar();
        }
        return System.nanoTime() - inicio;
    }

    private static void verificar(MazoPersonajes mazo, ListaInsercion lista, List<Personaje> entrada) {
        List<Personaje> esperado = new ArrayList<>(entrada);
        esperado.sort(GENERO);
        int posicion = 0;
        Nodo nodo = lista.cabeza;
        for (Personaje personaje : mazo) {
            if (posicion >= esperado.size() || personaje != esperado.get(posicion)
                    || nodo == null || nodo.dato != personaje
                    || mazo.buscarPorId(personaje.getId()) != personaje) {
                throw new AssertionError("Resultado incorrecto en posicion " + posicion);
            }
            nodo = nodo.siguiente;
            posicion++;
        }
        if (posicion != entrada.size() || nodo != null || mazo.getCantidad() != entrada.size()
                || (lista.cola != null && (lista.cola.siguiente != null
                || lista.cola.dato != esperado.get(esperado.size() - 1)))) {
            throw new AssertionError("Cantidad o cola incorrecta");
        }
    }

    private static void verificarCasosLimite() {
        List<Personaje> catalogo = new ArrayList<>();
        CatalogoPersonajes.crearCatalogo().forEach(catalogo::add);
        for (int cantidad : new int[] {0, 1, 18, 36}) {
            List<Personaje> entrada = new ArrayList<>(catalogo.subList(0, cantidad));
            MazoPersonajes mazo = crearMazo(entrada);
            ListaInsercion lista = new ListaInsercion(entrada);
            for (int repeticion = 0; repeticion < 2; repeticion++) {
                mazo.ordenarPorGenero();
                lista.ordenar();
                verificar(mazo, lista, entrada);
            }
        }
    }

    private static void fila(PrintWriter csv, String escenario, int tanda, String primero,
            String algoritmo, int cantidad, long nanos) {
        csv.printf(Locale.ROOT, "%s,%d,%s,%s,%d,%d,%.9f%n", escenario, tanda + 1,
                primero, algoritmo, cantidad, nanos, nanos / (cantidad * 1_000_000.0));
    }

    private static void resumen(String escenario, String algoritmo, double[] tiempos) {
        java.util.Arrays.sort(tiempos);
        System.out.printf(Locale.ROOT, "%s %s: mediana=%.9f ms; min=%.9f; max=%.9f%n",
                escenario, algoritmo, (tiempos[14] + tiempos[15]) / 2,
                tiempos[0], tiempos[tiempos.length - 1]);
    }

    private static final class ListaInsercion {
        private Nodo cabeza;
        private Nodo cola;

        ListaInsercion(List<Personaje> entrada) {
            for (Personaje personaje : entrada) {
                Nodo nuevo = new Nodo(personaje);
                if (cola == null) {
                    cabeza = nuevo;
                } else {
                    cola.siguiente = nuevo;
                }
                cola = nuevo;
            }
        }

        void ordenar() {
            Nodo ordenada = null;
            Nodo actual = cabeza;
            while (actual != null) {
                Nodo siguiente = actual.siguiente;
                if (ordenada == null || GENERO.compare(actual.dato, ordenada.dato) < 0) {
                    actual.siguiente = ordenada;
                    ordenada = actual;
                } else {
                    Nodo anterior = ordenada;
                    // Insertar después de los iguales conserva la estabilidad.
                    while (anterior.siguiente != null
                            && GENERO.compare(anterior.siguiente.dato, actual.dato) <= 0) {
                        anterior = anterior.siguiente;
                    }
                    actual.siguiente = anterior.siguiente;
                    anterior.siguiente = actual;
                }
                actual = siguiente;
            }
            cabeza = ordenada;
            // Igual que el método del juego, restablece la cola dentro de la medición.
            cola = cabeza;
            while (cola != null && cola.siguiente != null) {
                cola = cola.siguiente;
            }
        }
    }
}
