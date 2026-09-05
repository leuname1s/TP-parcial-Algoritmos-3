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
                output.reset();
                game.jugar();
                check(text().contains("HA ADIVINADO"), "Spectator game did not finish");
            }
        } finally {
            System.setOut(original);
        }
        System.out.println("PASS: secret visibility, arbiter API, 920 machine searches, "
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
        MazoPersonajes deck = CatalogoPersonajes.crearMazo();
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
                                ? new Maquina1(deck, previous) : new Maquina2(deck, previous);
                        seed(machine, seed);
                        boolean won = false;
                        int limit = kind == 1 ? 23 : 7;
                        for (int round = 0; round < limit && !won; round++) {
                            output.reset();
                            won = (Boolean) turn.invoke(referee, machine, target);
                            check(machine.getTablero().estaVivo(target.getId()), "Correct target discarded");
                            if (won) {
                                check(text().contains("El personaje era: " + target.getNombre()),
                                        "Machine declared the wrong winner");
                            }
                        }
                        check(won, "Machine failed to find target within its original turn limit");
                        check(previous.getCantidadViva() == originalCount, "Inherited board was mutated");
                    }
                }
            }
        }
    }

    private static void checkSecondPhase(ModoJuego mode) throws Exception {
        Random expected = new Random(9);
        int firstSecret = expected.nextInt(23) + 1;
        int secondSecret = expected.nextInt(23) + 1;
        // One question gives the first machine a turn before the player wins.
        String input = "1\n1\n1\n1\n2\n" + firstSecret + "\n1\n2\n" + secondSecret + "\n";
        Partida game = new Partida(new Scanner(input));
        seed(game, 9);
        game.iniciar(mode);
        Field firstField = Partida.class.getDeclaredField(
                mode == ModoJuego.JUGADOR_VS_MAQUINA_1 ? "maquina1" : "maquina2");
        firstField.setAccessible(true);
        IMaquina first = (IMaquina) firstField.get(game);
        // Fix the random first guess to a character other than the player's ID 1.
        MazoPersonajes deck = CatalogoPersonajes.crearMazo();
        int safeSeed = 0;
        while (true) {
            int index = new Random(safeSeed).nextInt(23);
            Personaje guess = null;
            for (Personaje candidate : deck) {
                if (index-- == 0) { guess = candidate; break; }
            }
            if (guess.getId() != 1) { break; }
            safeSeed++;
        }
        seed(first, safeSeed);
        output.reset();
        game.jugar();
        check(text().contains("INICIANDO FASE 2"), "Second phase missing");
        check(!text().contains("Objetivo secreto de"), "Second phase leaked a secret");
        check(game.getPersonajeJugador().getId() == 1, "Player secret changed");
        check(game.getTableroJugador().getCantidadViva() == 23, "Player board not reset");
        for (int id = 1; id <= 23; id++) {
            check(game.getTableroMaquina1().estaVivo(id) == game.getTableroMaquina2().estaVivo(id),
                    "Second machine did not inherit candidates");
        }
        check(text().contains("El personaje secreto era: " + deck.buscarPorId(secondSecret).getNombre()),
                "Second phase did not finish against the correct secret");
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
