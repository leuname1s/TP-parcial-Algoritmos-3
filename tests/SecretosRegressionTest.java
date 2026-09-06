import datos.ModoJuego;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;
import Funcionalidades.Maquina1;
import Funcionalidades.Maquina2;
import datos.MazoPersonajes;
import Funcionalidades.Partida;
import Funcionalidades.TableroCandidatos;
import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.Scanner;

public class SecretosRegressionTest {
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public static void main(String[] args) throws Exception {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            check(IMaquina.class.getMethod("ejecutarTurno", IArbitroTurno.class) != null,
                    "Machines must receive an arbiter");
            for (Method method : IMaquina.class.getMethods()) {
                for (Class<?> parameter : method.getParameterTypes()) {
                    check(parameter != Personaje.class, "Machine API exposes the secret");
                }
            }
            checkInitialVisibility();
            checkMachineTurns();
            checkSecondPhase(ModoJuego.JUGADOR_VS_MAQUINA_1);
            checkSecondPhase(ModoJuego.JUGADOR_VS_MAQUINA_2);
            for (int seed = 0; seed < 100; seed++) {
                Partida game = new Partida(new Scanner(""));
                seed(game, seed);
                game.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
                check(game.getObjetivoMaquina1().getId() != game.getObjetivoMaquina2().getId(),
                        "Spectator machines share a secret");
                check(game.getTableroMaquina1().estaVivo(game.getObjetivoMaquina2().getId()),
                        "Spectator target absent from deck");
                output.reset();
                game.jugar();
                check(text().contains("HA ADIVINADO"), "Spectator game did not finish");
            }
        } finally {
            System.setOut(original);
        }
        System.out.println("PASS: secret visibility, arbiter API, 1440 machine searches, "
                + "both second-phase orders and 100 spectator games");
    }

    private static void checkInitialVisibility() throws Exception {
        for (ModoJuego mode : new ModoJuego[] {ModoJuego.JUGADOR_VS_MAQUINA_1,
                ModoJuego.JUGADOR_VS_MAQUINA_2, ModoJuego.MAQUINA_1_VS_MAQUINA_2}) {
            Partida game = new Partida(new Scanner("2\n"));
            output.reset();
            game.iniciar(mode);
            boolean spectator = mode == ModoJuego.MAQUINA_1_VS_MAQUINA_2;
            check(text().contains("Objetivo secreto de") == spectator,
                    "Incorrect secret visibility in " + mode);
            if (!spectator) {
                check(text().contains("Tu personaje secreto:"), "Own secret is missing");
            }
        }
    }

    private static void checkMachineTurns() throws Exception {
        MazoPersonajes deck = CatalogoPersonajes.crearCatalogo();
        Partida referee = new Partida(new Scanner(""));
        Method turn = Partida.class.getDeclaredMethod("ejecutarTurnoMaquina", IMaquina.class, Personaje.class);
        turn.setAccessible(true);
        for (Personaje target : deck) {
            for (int seed = 0; seed < 10; seed++) {
                for (int kind = 1; kind <= 2; kind++) {
                    for (boolean inherited : new boolean[] {false, true}) {
                        TableroCandidatos previous = new TableroCandidatos(deck);
                        if (inherited) {
                            previous.descartarSegun(Pregunta.USA_LENTES, Pregunta.USA_LENTES.cumple(target));
                        }
                        int originalCount = previous.getCantidadViva();
                        IMaquina machine = kind == 1
                                ? new Maquina1(deck, previous, new Random(seed))
                                : new Maquina2(deck, previous, new Random(seed));
                        boolean won = false;
                        // Each unsuccessful turn must eliminate at least one candidate.
                        int limit = originalCount;
                        for (int round = 0; round < limit && !won; round++) {
                            output.reset();
                            int before = machine.getTablero().getCantidadViva();
                            won = (Boolean) turn.invoke(referee, machine, target);
                            check(machine.getTablero().estaVivo(target.getId()), "Correct target discarded");
                            check(won || machine.getTablero().getCantidadViva() < before,
                                    "Unsuccessful turn made no progress");
                            if (won) {
                                check(text().contains("El personaje era: " + target.getNombre()),
                                        "Machine declared the wrong winner");
                            }
                        }
                        check(won, "Machine failed to find target within its strategy turn limit");
                        check(previous.getCantidadViva() == originalCount, "Inherited board was mutated");
                    }
                }
            }
        }
    }

    private static void checkSecondPhase(ModoJuego mode) throws Exception {
        Random expected = new Random(9);
        MazoPersonajes deck = CatalogoPersonajes.crearMazo(expected);
        Personaje player = deck.iterator().next();
        Personaje firstSecret = select(deck, expected, null);
        Personaje secondSecret = select(deck, expected, firstSecret);
        String input = "1\n" + player.getId() + "\n1\n1\n2\n" + firstSecret.getId()
                + "\n1\n2\n" + secondSecret.getId() + "\n1\n";
        Partida game = new Partida(new Scanner(input), new Random(9));
        game.iniciar(mode);
        Field firstField = Partida.class.getDeclaredField(
                mode == ModoJuego.JUGADOR_VS_MAQUINA_1 ? "maquina1" : "maquina2");
        firstField.setAccessible(true);
        IMaquina first = (IMaquina) firstField.get(game);
        // Force a failed guess so both strategies leave the optional phase available.
        Random failedGuess = new Random(0) {
            private static final long serialVersionUID = 1L;
            @Override public double nextDouble() { return 0.0; }
            @Override public int nextInt(int bound) { return 1; }
        };
        first = mode == ModoJuego.JUGADOR_VS_MAQUINA_1
                ? new Maquina1(deck, (TableroCandidatos) first.getTablero(), failedGuess)
                : new Maquina2(deck, (TableroCandidatos) first.getTablero(), failedGuess);
        firstField.set(game, first);
        output.reset();
        game.jugar();
        check(text().contains("INICIANDO FASE 2"), "Second phase missing");
        check(!text().contains("Objetivo secreto de"), "Second phase leaked a secret");
        check(game.getPersonajeJugador().getId() == player.getId(), "Player secret changed");
        check(game.getObjetivoMaquina1().getId() != game.getObjetivoMaquina2().getId(),
                "Machines share a secret across phases");
        check(game.getTableroJugador().getCantidadViva() == 22, "Player board must start with 22 candidates");
        check(!game.getTableroJugador().estaVivo(firstSecret.getId()), "Previous secret was not excluded");
        for (int id = 1; id <= CatalogoPersonajes.TOTAL_CATALOGO; id++) {
            check(game.getTableroMaquina1().estaVivo(id) == game.getTableroMaquina2().estaVivo(id),
                    "Second machine did not inherit candidates");
            check(game.getTableroJugador().estaVivo(id)
                            == (deck.buscarPorId(id) != null && id != firstSecret.getId()),
                    "Deck changed between phases");
        }
        check(text().contains("El personaje secreto era: " + secondSecret.getNombre()),
                "Second phase did not finish against the correct secret");
        Method question = Partida.class.getDeclaredMethod("hacerPreguntaJugador", Personaje.class);
        question.setAccessible(true);
        output.reset();
        check((Boolean) question.invoke(game, secondSecret), "Question unavailable for new secret");
        check(!text().contains("Ya realizaste esa pregunta"), "Question history was not reset");
    }

    private static Personaje select(MazoPersonajes deck, Random random, Personaje excluded) {
        int index = random.nextInt(deck.getCantidad() - (excluded == null ? 0 : 1));
        for (Personaje p : deck) {
            if (!p.equals(excluded) && index-- == 0) {
                return p;
            }
        }
        throw new AssertionError("No character selected");
    }

    private static void seed(Object object, long value) throws Exception {
        Field random = object.getClass().getDeclaredField("random");
        random.setAccessible(true);
        ((Random) random.get(object)).setSeed(value);
    }

    private static String text() throws Exception {
        return output.toString("UTF-8");
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
