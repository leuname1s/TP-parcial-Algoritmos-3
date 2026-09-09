import datos.MazoPersonajes;
import datos.Personaje;
import defaults.CatalogoPersonajes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class OrdenamientoMazoRegressionTest {
    public static void main(String[] args) {
        List<Personaje> catalog = new ArrayList<>();
        for (Personaje p : CatalogoPersonajes.crearCatalogo()) { catalog.add(p); }
        verify(new ArrayList<>());
        verify(new ArrayList<>(catalog.subList(0, 1)));
        verify(new ArrayList<>(catalog.subList(0, 18)));
        verify(new ArrayList<>(catalog.subList(18, 36)));
        verify(new ArrayList<>(catalog));
        Collections.reverse(catalog);
        verify(new ArrayList<>(catalog));
        for (int seed = 0; seed < 100; seed++) {
            List<Personaje> shuffled = new ArrayList<>();
            for (Personaje p : CatalogoPersonajes.crearCatalogo()) { shuffled.add(p); }
            Collections.shuffle(shuffled, new Random(seed));
            for (int size = 0; size <= shuffled.size(); size++) {
                verify(new ArrayList<>(shuffled.subList(0, size)));
            }
            List<Personaje> expected = new ArrayList<>(shuffled.subList(0, MazoPersonajes.TOTAL));
            expected.sort(Comparator.comparingInt(p -> p.getGenero().getOrden()));
            int index = 0;
            for (Personaje p : CatalogoPersonajes.crearMazo(new Random(seed))) {
                check(p.getId() == expected.get(index++).getId(), "La creación del mazo cambió la selección o el orden estable");
            }
            check(index == MazoPersonajes.TOTAL, "La creación del mazo perdió personajes");
        }
        System.out.println("PASS: MergeSort estable, tamaños 0..36, identidad, ID, ordenamiento repetido y agregado posterior");
    }

    private static void verify(List<Personaje> input) {
        MazoPersonajes deck = new MazoPersonajes(36);
        for (Personaje p : input) { deck.agregar(p); }
        assertSequence(deck, input);
        List<Personaje> expected = new ArrayList<>(input);
        expected.sort(Comparator.comparingInt(p -> p.getGenero().getOrden()));
        deck.ordenarPorGenero();
        assertSequence(deck, expected);
        deck.ordenarPorGenero();
        assertSequence(deck, expected);
        for (Personaje p : CatalogoPersonajes.crearCatalogo()) {
            if (deck.buscarPorId(p.getId()) == null) {
                deck.agregar(p);
                expected.add(p);
                assertSequence(deck, expected);
                expected.sort(Comparator.comparingInt(value -> value.getGenero().getOrden()));
                deck.ordenarPorGenero();
                assertSequence(deck, expected);
                break;
            }
        }
    }

    private static void assertSequence(MazoPersonajes deck, List<Personaje> expected) {
        check(deck.getCantidad() == expected.size(), "Cambió la cantidad");
        int index = 0;
        for (Personaje p : deck) {
            check(index < expected.size(), "Ciclo o nodo adicional");
            check(p == expected.get(index++), "Cambió el orden, la estabilidad o la identidad");
            check(deck.buscarPorId(p.getId()) == p, "Cambió el índice por ID");
        }
        check(index == expected.size(), "Falta un nodo");
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
