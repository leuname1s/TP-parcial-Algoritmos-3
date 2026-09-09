package Funcionalidades;

import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import Interfaces.ITableroCandidatos;
import datos.ColorPelo;
import datos.Genero;
import datos.MazoPersonajes;
import datos.ModoJuego;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class EstrategiasPartidaRegressionTest {
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public static void main(String[] args) throws Exception {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            checkQuestions();
            checkRisk();
            checkGuessSelection();
            checkSingleAction();
            for (ModoJuego mode : new ModoJuego[] {
                    ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2}) {
                for (int discarded : new int[] {0, 14, 15, 22}) {
                    checkPhase(mode, discarded, true);
                }
                checkPhase(mode, 14, false);
                checkFirstPhaseLoss(mode);
            }
        } finally {
            System.setOut(original);
        }
        System.out.println("PASS: risk thresholds, inherited risk, adaptive questions, random ties, "
                + "single-action turns, phase boundaries, declined challenge and first-phase losses");
    }

    private static void checkQuestions() {
        MazoPersonajes deck = new MazoPersonajes();
        deck.agregar(new Personaje(2, "A", Genero.FEMENINO, ColorPelo.NEGRO, false, false, false));
        deck.agregar(new Personaje(7, "B", Genero.FEMENINO, ColorPelo.NEGRO, true, false, false));
        deck.agregar(new Personaje(18, "C", Genero.FEMENINO, ColorPelo.PELIRROJO, true, false, false));
        deck.agregar(new Personaje(36, "D", Genero.MASCULINO, ColorPelo.PELIRROJO, false, false, false));
        TableroCandidatos board = new TableroCandidatos(deck);
        checkTies(EstrategiaMaquina.AGRESIVA, board,
                EnumSet.of(Pregunta.ES_FEMENINO, Pregunta.ES_MASCULINO));
        checkTies(EstrategiaMaquina.EQUILIBRADA, board,
                EnumSet.of(Pregunta.PELO_NEGRO, Pregunta.PELO_PELIRROJO, Pregunta.USA_LENTES));

        // The inherited gender answer is constant and must not be asked again.
        board.descartar(36);
        for (EstrategiaMaquina strategy : EstrategiaMaquina.values()) {
            for (int index = 0; index < 3; index++) {
                Pregunta question = strategy.seleccionarPregunta(board, new FixedRandom(0.9, index));
                check(question != Pregunta.ES_FEMENINO && question != Pregunta.ES_MASCULINO,
                        "Inherited constant question selected");
            }
        }
    }

    private static void checkTies(EstrategiaMaquina strategy, TableroCandidatos board,
            EnumSet<Pregunta> expected) {
        EnumSet<Pregunta> selected = EnumSet.noneOf(Pregunta.class);
        for (int index = 0; index < expected.size(); index++) {
            selected.add(strategy.seleccionarPregunta(board, new FixedRandom(0.9, index)));
        }
        check(selected.equals(expected), "Wrong optimal questions or unreachable tie");
        check(board.getCantidadViva() == 4, "Selecting a question mutated the board");
    }

    private static void checkRisk() {
        int[] discarded = {0, 1, 10, 14, 20, 21};
        double[][] expected = {{4, 9, 54, 74, 100, 100}, {4, 6.5, 29, 39, 54, 56.5}};
        MazoPersonajes deck = CatalogoPersonajes.crearMazo(new Random(42));
        Personaje target = deck.iterator().next();
        for (int kind = 1; kind <= 2; kind++) {
            EstrategiaMaquina strategy = kind == 1
                    ? EstrategiaMaquina.AGRESIVA : EstrategiaMaquina.EQUILIBRADA;
            for (int i = 0; i < discarded.length; i++) {
                double percentage = expected[kind - 1][i];
                check(strategy.porcentajeRiesgo(discarded[i]) == percentage, "Wrong risk formula");
                TableroCandidatos inherited = boardWithDiscards(deck, discarded[i], target.getId());
                double threshold = percentage / 100.0;
                for (double draw : new double[] {threshold - 0.000001, threshold}) {
                    if (draw >= 1.0) { continue; }
                    IMaquina machine = machine(kind, deck, inherited, new FixedRandom(draw, 0));
                    RecordingArbiter arbiter = new RecordingArbiter(target);
                    machine.ejecutarTurno(arbiter);
                    check(arbiter.guesses == (draw < threshold ? 1 : 0),
                            "Wrong risk boundary: machine " + kind + ", discarded " + discarded[i]
                                    + ", draw " + draw);
                    check(arbiter.questions + arbiter.guesses == 1, "Turn must perform one action");
                    check(inherited.getCantidadViva() == 23 - discarded[i], "Inherited board mutated");
                    check(machine.getTablero().estaVivo(target.getId()), "Correct target discarded");
                }
            }
        }
    }

    private static void checkGuessSelection() {
        MazoPersonajes deck = CatalogoPersonajes.crearMazo(new Random(42));
        Personaje target = deck.iterator().next();
        TableroCandidatos board = boardWithDiscards(deck, 10, target.getId());
        List<Integer> live = new ArrayList<>();
        for (Personaje character : deck) {
            if (board.estaVivo(character.getId())) { live.add(character.getId()); }
        }
        for (int kind = 1; kind <= 2; kind++) {
            for (int index = 0; index < live.size(); index++) {
                IMaquina machine = machine(kind, deck, board, new FixedRandom(0.0, index));
                RecordingArbiter arbiter = new RecordingArbiter(target);
                boolean won = machine.ejecutarTurno(arbiter);
                check(arbiter.lastGuess == live.get(index), "Guess did not use the uniform live index");
                check(won == (arbiter.lastGuess == target.getId()), "Incorrect guess result");
                check(machine.getTablero().getCantidadViva() == (won ? 13 : 12),
                        "Failed guess must discard exactly one candidate");
            }
        }
    }

    private static void checkSingleAction() {
        MazoPersonajes deck = new MazoPersonajes();
        Personaje target = new Personaje(2, "A", Genero.FEMENINO, ColorPelo.NEGRO, false, false, false);
        deck.agregar(target);
        deck.agregar(new Personaje(36, "B", Genero.MASCULINO, ColorPelo.NEGRO, false, false, false));
        for (int kind = 1; kind <= 2; kind++) {
            IMaquina machine = machine(kind, deck, new TableroCandidatos(deck), new FixedRandom(0.99, 0));
            RecordingArbiter arbiter = new RecordingArbiter(target);
            check(!machine.ejecutarTurno(arbiter), "Question incorrectly won in the same turn");
            check(arbiter.questions == 1 && arbiter.guesses == 0, "Question and guess in one turn");
            check(machine.getTablero().getCantidadViva() == 1, "Expected one survivor");
            check(machine.ejecutarTurno(arbiter), "Single survivor was not guessed");
            check(arbiter.questions == 1 && arbiter.guesses == 1, "Single survivor must skip questions");
        }

        // No useful question is a fallback; valid distinguishable profiles cannot normally reach it.
        TableroCandidatos noQuestions = new TableroCandidatos(deck) {
            @Override public int contarSiCumplen(Pregunta question) { return 0; }
        };
        for (EstrategiaMaquina strategy : EstrategiaMaquina.values()) {
            RecordingArbiter arbiter = new RecordingArbiter(target);
            check(strategy.ejecutarTurno("Test", deck, noQuestions, new FixedRandom(0.99, 0), arbiter),
                    "No-question fallback failed to guess");
            check(arbiter.questions == 0 && arbiter.guesses == 1, "Fallback asked a constant question");
        }
    }

    private static void checkPhase(ModoJuego mode, int discarded, boolean accept) throws Exception {
        Random expected = new Random(9);
        MazoPersonajes deck = CatalogoPersonajes.crearMazo(expected);
        Personaje player = deck.iterator().next();
        Personaje firstSecret = select(deck, expected, null);
        Personaje secondSecret = select(deck, expected, firstSecret);
        boolean eligible = discarded < 15;
        String input = "1\n" + player.getId() + "\n2\n" + firstSecret.getId() + "\n"
                + (eligible ? (accept ? "1\n2\n" + secondSecret.getId() + "\n" : "2\n") : "")
                + "sentinel\n";
        Scanner scanner = new Scanner(input);
        Partida game = new Partida(scanner, new Random(9));
        game.iniciar(mode);
        ITableroCandidatos first = mode == ModoJuego.JUGADOR_VS_MAQUINA_1
                ? game.getTableroMaquina1() : game.getTableroMaquina2();
        discard(first, deck, discarded, player.getId());
        output.reset();
        game.jugar();
        String log = output.toString("UTF-8");
        check(log.contains("¿Deseas continuar") == eligible, "Wrong phase eligibility at " + discarded);
        check(log.contains("INICIANDO FASE 2") == (eligible && accept), "Wrong phase transition");
        check(scanner.nextLine().equals("sentinel"), "Unexpected prompt consumed another input");
        if (eligible && accept) {
            ITableroCandidatos second = mode == ModoJuego.JUGADOR_VS_MAQUINA_1
                    ? game.getTableroMaquina2() : game.getTableroMaquina1();
            check(first != second, "Inherited board is aliased");
            check(second.getCantidadViva() == 23 - discarded, "Wrong inherited count");
            check(game.getPersonajeJugador().getId() == player.getId(), "Player secret changed");
            check(game.getTableroJugador().getCantidadViva() == 22, "Human board must have 22 options");
            for (int id = 1; id <= CatalogoPersonajes.TOTAL_CATALOGO; id++) {
                check(first.estaVivo(id) == second.estaVivo(id), "Wrong inherited candidate set");
                check(game.getTableroJugador().estaVivo(id)
                        == (deck.buscarPorId(id) != null && id != firstSecret.getId()),
                        "Human board changed deck or excluded wrong secret");
            }
            second.descartar(player.getId());
            check(first.estaVivo(player.getId()), "Second board mutation reached first board");
        }
    }

    private static void checkFirstPhaseLoss(ModoJuego mode) throws Exception {
        Random expected = new Random(9);
        MazoPersonajes deck = CatalogoPersonajes.crearMazo(expected);
        Personaje player = deck.iterator().next();
        Personaje secret = select(deck, expected, null);
        Personaje wrong = null;
        for (Personaje character : deck) {
            if (!character.equals(secret)) { wrong = character; break; }
        }
        Scanner scanner = new Scanner("1\n" + player.getId() + "\n2\n" + wrong.getId() + "\nsentinel\n");
        Partida game = new Partida(scanner, new Random(9));
        game.iniciar(mode);
        Field field = Partida.class.getDeclaredField(
                mode == ModoJuego.JUGADOR_VS_MAQUINA_1 ? "maquina1" : "maquina2");
        field.setAccessible(true);
        field.set(game, machine(mode == ModoJuego.JUGADOR_VS_MAQUINA_1 ? 1 : 2,
                deck, new TableroCandidatos(deck), new FixedRandom(0.0, 0)));
        output.reset();
        game.jugar();
        String log = output.toString("UTF-8");
        check(log.contains("HA ADIVINADO") && !log.contains("¿Deseas continuar")
                && !log.contains("INICIANDO FASE 2"), "Loss offered a second phase");
        check(scanner.nextLine().equals("sentinel"), "Loss consumed unexpected input");
    }

    private static IMaquina machine(int kind, MazoPersonajes deck, TableroCandidatos board, Random random) {
        return kind == 1 ? new Maquina1(deck, board, random) : new Maquina2(deck, board, random);
    }

    private static TableroCandidatos boardWithDiscards(MazoPersonajes deck, int count, int target) {
        TableroCandidatos board = new TableroCandidatos(deck);
        discard(board, deck, count, target);
        return board;
    }

    private static void discard(ITableroCandidatos board, MazoPersonajes deck, int count, int target) {
        for (Personaje character : deck) {
            if (count == 0) { break; }
            if (character.getId() != target) {
                board.descartar(character.getId());
                count--;
            }
        }
        check(count == 0, "Fixture could not discard requested candidates");
    }

    private static Personaje select(MazoPersonajes deck, Random random, Personaje excluded) {
        List<Personaje> available = new ArrayList<>();
        for (Personaje character : deck) {
            if (!character.equals(excluded)) { available.add(character); }
        }
        return available.get(random.nextInt(available.size()));
    }

    private static class FixedRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final double draw;
        private final int index;
        FixedRandom(double draw, int index) {
            super(0);
            this.draw = draw;
            this.index = index;
        }
        @Override public double nextDouble() { return draw; }
        @Override public int nextInt(int bound) {
            check(index >= 0 && index < bound, "Invalid random index for candidates or ties");
            return index;
        }
    }

    private static class RecordingArbiter implements IArbitroTurno {
        private final Personaje target;
        private int questions;
        private int guesses;
        private int lastGuess;
        RecordingArbiter(Personaje target) { this.target = target; }
        @Override public boolean responder(Pregunta question) {
            questions++;
            return question.cumple(target);
        }
        @Override public boolean comprobarIntento(int id) {
            guesses++;
            lastGuess = id;
            return id == target.getId();
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
