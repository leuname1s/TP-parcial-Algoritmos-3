import datos.ColorPelo;
import datos.Genero;
import datos.MazoPersonajes;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;
import Funcionalidades.TableroCandidatos;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PersonajesMazoRegressionTest {
    public static void main(String[] args) {
        MazoPersonajes catalog = CatalogoPersonajes.crearCatalogo();
        check(catalog.getCantidad() == 36 && catalog.estaCompleto(), "Incomplete catalog");
        Set<String> profiles = new HashSet<>();
        int female = 0;
        for (Personaje p : catalog) {
            check(profiles.add(answers(p)), "Characters cannot be distinguished by questions");
            if (p.getGenero() == Genero.FEMENINO) { female++; }
            check(Pregunta.ES_CALVO.cumple(p) == (p.getColorPelo() == ColorPelo.PELADO),
                    "Incorrect baldness semantics");
            if (p.getColorPelo() == ColorPelo.PELADO) {
                check(!Pregunta.PELO_NEGRO.cumple(p) && !Pregunta.PELO_RUBIO.cumple(p)
                        && !Pregunta.PELO_PELIRROJO.cumple(p), "Bald character has a hair color");
            }
            TableroCandidatos board = new TableroCandidatos(catalog);
            for (Pregunta q : Pregunta.values()) {
                board.descartarSegun(q, q.cumple(p));
                check(board.estaVivo(p.getId()), "Filter discarded correct target");
            }
            check(board.getCantidadViva() == 1 && board.unicoSobreviviente().equals(p),
                    "Questions do not isolate the target");
        }
        check(female == 18, "Unexpected gender distribution");
        check(catalog.buscarPorId(1).getNombre().equals("Isabella")
                && catalog.buscarPorId(36).getNombre().equals("Santiago"), "Unstable catalog IDs");

        Set<Integer> seen = new HashSet<>();
        Set<String> selections = new HashSet<>();
        for (int seed = 0; seed < 100; seed++) {
            MazoPersonajes deck = CatalogoPersonajes.crearMazo(new Random(seed));
            MazoPersonajes repeated = CatalogoPersonajes.crearMazo(new Random(seed));
            check(deck.getCantidad() == 23 && deck.estaCompleto(), "Wrong deck size");
            Set<Integer> ids = new HashSet<>();
            int previousGender = -1;
            StringBuilder selection = new StringBuilder();
            TableroCandidatos board = new TableroCandidatos(deck);
            for (Personaje p : deck) {
                check(ids.add(p.getId()), "Duplicate ID in sample");
                seen.add(p.getId());
                selection.append(p.getId()).append(',');
                check(p.getGenero().getOrden() >= previousGender, "Deck is not ordered by gender");
                previousGender = p.getGenero().getOrden();
                check(p.toString().equals(catalog.buscarPorId(p.getId()).toString()), "Character changed");
                check(repeated.buscarPorId(p.getId()) != null, "Seed is not reproducible");
            }
            selections.add(selection.toString());
            for (int id = -1; id <= 38; id++) {
                check(board.estaVivo(id) == ids.contains(id), "Absent ID is marked alive");
            }
            Personaje first = deck.iterator().next();
            TableroCandidatos copy = new TableroCandidatos(board);
            copy.descartar(first.getId());
            copy.descartar(first.getId());
            copy.descartar(999);
            check(copy.getCantidadViva() == 22 && board.getCantidadViva() == 23,
                    "Copy shares state or repeated discard changes count");
        }
        check(seen.size() == 36 && selections.size() > 1, "Sampling is stuck on a fixed subset");
        checkInsertion();
        System.out.println("PASS: 36 distinguishable profiles, 100 sampled decks, sparse IDs and independent boards");
    }

    private static void checkInsertion() {
        MazoPersonajes deck = new MazoPersonajes(2);
        Personaje male = new Personaje(36, "M", Genero.MASCULINO, ColorPelo.PELADO, false, false, true);
        Personaje female = new Personaje(2, "F", Genero.FEMENINO, ColorPelo.NEGRO, true, true, false);
        deck.agregar(male);
        expect(IllegalArgumentException.class, () -> deck.agregar(male));
        expect(IllegalArgumentException.class, () -> deck.agregar(new Personaje(35, "Duplicate",
                Genero.MASCULINO, ColorPelo.PELADO, false, false, true)));
        check(deck.getCantidad() == 1, "Rejected insertion changed deck");
        deck.agregar(female);
        check(deck.iterator().next() == male, "El agregado debe conservar el orden de carga");
        deck.ordenarPorGenero();
        check(deck.iterator().next() == female && deck.buscarPorId(36) == male,
                "Insertion changed ID or gender order");
        check(deck.buscarPorId(1) == null && deck.buscarPorId(37) == null, "Invalid lookup");
        expect(IllegalStateException.class, () -> deck.agregar(new Personaje(3, "Overflow",
                Genero.FEMENINO, ColorPelo.RUBIO, false, false, false)));
    }

    private static String answers(Personaje p) {
        StringBuilder result = new StringBuilder();
        for (Pregunta q : Pregunta.values()) { result.append(q.cumple(p) ? '1' : '0'); }
        return result.toString();
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable operation) {
        try { operation.run(); }
        catch (RuntimeException error) {
            check(type.isInstance(error), "Unexpected exception: " + error);
            return;
        }
        throw new AssertionError("Missing expected exception: " + type.getName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
