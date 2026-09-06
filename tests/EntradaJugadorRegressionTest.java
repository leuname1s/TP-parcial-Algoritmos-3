import datos.MazoPersonajes;
import datos.ModoJuego;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;
import Funcionalidades.Partida;
import Funcionalidades.TableroCandidatos;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.Scanner;

public class EntradaJugadorRegressionTest {
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public static void main(String[] args) throws Exception {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            checkRepeatedQuestions();
            checkInvalidGuesses();
            checkFilteredGuess();
            checkExhaustedQuestions();
            checkManualSelectionAndRestart();
        } finally {
            System.setOut(original);
        }
        System.out.println("PASS: repeated questions, absent/discarded IDs, exhausted questions and game restart");
    }

    private static void checkRepeatedQuestions() throws Exception {
        Partida game = fixture("1\n1\n1\n1\n8\n2\n30\n");
        Personaje target = CatalogoPersonajes.crearCatalogo().buscarPorId(30);
        check(!turn(game, target), "Question should not win");
        output.reset();
        check(!turn(game, target), "Second valid question should not win");
        check(text().contains("Ya realizaste esa pregunta"), "Repeated question accepted");
        check(count(text(), "Pregunta realizada:") == 1, "Repeated question was evaluated again");
        check(game.getTableroJugador().estaVivo(30), "Correct target discarded");
        check(turn(game, target), "Rejected question consumed or corrupted a turn");
    }

    private static void checkInvalidGuesses() throws Exception {
        Partida game = fixture("2\n2\n2\nabc\n0\n37\n1\n2\n36\n");
        Personaje target = CatalogoPersonajes.crearCatalogo().buscarPorId(36);
        check(!turn(game, target), "Wrong guess won");
        check(!game.getTableroJugador().estaVivo(2), "Failed guess was not discarded");
        output.reset();
        check(turn(game, target), "Invalid ID consumed the turn before valid winning guess");
        check(text().contains("Entrada no v") && text().contains("no pertenece a esta partida")
                        && text().contains("ya fue descartado"), "Missing rejection feedback");
        check(count(text(), "INCORRECTO") == 0, "Rejected input evaluated as a guess");
        check(game.getTableroJugador().getCantidadViva() == 2, "Rejected input changed candidates");
    }

    private static void checkFilteredGuess() throws Exception {
        Partida game = fixture("1\n1\n2\n2\n36\n");
        Personaje target = CatalogoPersonajes.crearCatalogo().buscarPorId(36);
        check(!turn(game, target), "Question should not win");
        check(!game.getTableroJugador().estaVivo(2), "Question did not discard mismatching candidate");
        output.reset();
        check(turn(game, target), "Guess for a filtered character consumed a turn");
        check(text().contains("ya fue descartado"), "Filtered character was accepted");
    }

    private static void checkExhaustedQuestions() throws Exception {
        StringBuilder input = new StringBuilder();
        for (int i = 1; i <= Pregunta.values().length; i++) { input.append("1\n").append(i).append('\n'); }
        input.append("1\n2\n36\n");
        Partida game = fixture(input.toString());
        Personaje target = CatalogoPersonajes.crearCatalogo().buscarPorId(36);
        for (Pregunta ignored : Pregunta.values()) { check(!turn(game, target), "Question should not win"); }
        output.reset();
        check(turn(game, target), "No way to leave the exhausted question menu in the same turn");
        check(text().contains("Ya realizaste todas las preguntas"), "Missing exhausted-menu feedback");
    }

    private static void checkManualSelectionAndRestart() throws Exception {
        MazoPersonajes expected = CatalogoPersonajes.crearMazo(new Random(42));
        int absent = 1;
        while (expected.buscarPorId(absent) != null) { absent++; }
        int present = expected.iterator().next().getId();
        Partida game = new Partida(new Scanner("1\n" + absent + "\n" + present + "\n1\n2\n1\n"),
                new Random(42));
        output.reset();
        game.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_1);
        check(game.getPersonajeJugador().getId() == present, "Manual selection accepted absent ID");
        check(text().contains("no pertenece a esta partida"), "Manual selection did not reject absent ID");
        Method question = Partida.class.getDeclaredMethod("hacerPreguntaJugador", Personaje.class);
        question.setAccessible(true);
        check((Boolean) question.invoke(game, game.getObjetivoMaquina1()), "First question unavailable");
        game.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_2);
        check(game.getObjetivoMaquina1() == null && game.getTableroMaquina1() == null,
                "Previous opponent survived game restart");
        output.reset();
        check((Boolean) question.invoke(game, game.getObjetivoMaquina2()), "Question not reset in new game");
        check(!text().contains("Ya realizaste esa pregunta"), "Question history survived game restart");
    }

    private static Partida fixture(String input) throws Exception {
        MazoPersonajes catalog = CatalogoPersonajes.crearCatalogo();
        MazoPersonajes deck = new MazoPersonajes();
        deck.agregar(catalog.buscarPorId(2));
        deck.agregar(catalog.buscarPorId(30));
        deck.agregar(catalog.buscarPorId(36));
        Partida game = new Partida(new Scanner(input), new Random(1));
        set(game, "mazo", deck);
        set(game, "tableroJugador", new TableroCandidatos(deck));
        output.reset();
        return game;
    }

    private static boolean turn(Partida game, Personaje target) throws Exception {
        Method turn = Partida.class.getDeclaredMethod("turnoJugador", Personaje.class);
        turn.setAccessible(true);
        return (Boolean) turn.invoke(game, target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static int count(String text, String token) {
        return (text.length() - text.replace(token, "").length()) / token.length();
    }

    private static String text() throws Exception { return output.toString("UTF-8"); }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
