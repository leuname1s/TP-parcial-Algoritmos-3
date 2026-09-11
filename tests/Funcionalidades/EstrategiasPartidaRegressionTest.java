package Funcionalidades;

import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import Interfaces.ITableroCandidatos;
import datos.ColorPelo;
import datos.EstadoPartida;
import datos.Genero;
import datos.MazoPersonajes;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoPartida;
import datos.ResultadoPartida.Desenlace;
import defaults.CatalogoPersonajes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.rechaza;

public class EstrategiasPartidaRegressionTest {
    public static void main(String[] args) {
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
        System.out.println("PASS: riesgo, herencia, preguntas adaptativas, desempates, una acción y límites de fase");
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

        // La respuesta heredada de género es constante y no debe volver a preguntarse.
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
                boolean won = machine.ejecutarTurno(arbiter).isAcierto();
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
            check(!machine.ejecutarTurno(arbiter).isAcierto(), "Question incorrectly won in the same turn");
            check(arbiter.questions == 1 && arbiter.guesses == 0, "Question and guess in one turn");
            check(machine.getTablero().getCantidadViva() == 1, "Expected one survivor");
            check(machine.ejecutarTurno(arbiter).isAcierto(), "Single survivor was not guessed");
            check(arbiter.questions == 1 && arbiter.guesses == 1, "Single survivor must skip questions");
        }

        // Defensa ante tableros sin preguntas útiles, aunque los perfiles normales son distinguibles.
        TableroCandidatos noQuestions = new TableroCandidatos(deck) {
            @Override public int contarSiCumplen(Pregunta question) { return 0; }
        };
        for (EstrategiaMaquina strategy : EstrategiaMaquina.values()) {
            RecordingArbiter arbiter = new RecordingArbiter(target);
            check(strategy.ejecutarTurno("Test", deck, noQuestions, new FixedRandom(0.99, 0), arbiter).isAcierto(),
                    "No-question fallback failed to guess");
            check(arbiter.questions == 0 && arbiter.guesses == 1, "Fallback asked a constant question");
        }
    }


    private static void checkPhase(ModoJuego mode, int discarded, boolean accept) {
        PartidaPrueba caso = new PartidaPrueba(mode, true);
        Partida game = caso.partida;
        caso.avanzarFallos(discarded);
        Participante firstMachine = game.getRivalActual();
        List<Personaje> first = game.getCandidatos(firstMachine);
        check(first.size() == 23 - discarded, "Descartes iniciales incorrectos");
        check(game.arriesgar(caso.primerSecreto.getId()).isAcierto(), "No ganó el humano");
        boolean eligible = discarded < 15;
        check((game.getEstado() == EstadoPartida.DECISION_SEGUNDA_FASE) == eligible, "Límite de fase incorrecto");
        if (eligible) {
            check(game.getResultado() == null, "Victoria intermedia registrada como final");
            rechaza(IllegalStateException.class, () -> game.preguntar(Pregunta.USA_LENTES));
            rechaza(IllegalStateException.class, game::ejecutarTurnoMaquina);
            game.decidirSegundaFase(accept);
        } else {
            rechaza(IllegalStateException.class, () -> game.decidirSegundaFase(true));
        }
        if (eligible && accept) {
            Participante secondMachine = game.getRivalActual();
            List<Personaje> second = game.getCandidatos(secondMachine);
            check(second.equals(first), "No heredó los candidatos");
            check(game.getPersonajeJugador().equals(caso.humano), "Cambió el secreto humano");
            check(game.getCandidatos(Participante.JUGADOR).size() == 22, "No hay 22 candidatos humanos");
            check(!game.getCandidatos(Participante.JUGADOR).contains(caso.primerSecreto), "No excluyó el secreto anterior");
            check(game.getPreguntasRealizadas().isEmpty(), "No reinició preguntas");
            check(game.getFase() == 2 && game.getRonda() == 1 && game.getTurno() == Participante.JUGADOR,
                    "Inicio de fase incorrecto");
            rechaza(UnsupportedOperationException.class, second::clear);
            // Se comprueba la independencia jugando, sin exponer ni mutar el tablero interno.
            caso.fallarJugador();
            check(!game.ejecutarTurnoMaquina().isAcierto(), "La segunda máquina debía fallar");
            check(game.getCandidatos(firstMachine).equals(first), "Segunda máquina alteró el tablero anterior");
            check(game.getCandidatos(secondMachine).size() == first.size() - 1, "Segunda máquina no avanzó");
            check(second.equals(first), "La consulta anterior cambió retroactivamente");
            check(game.arriesgar(caso.segundoSecreto.getId()).isAcierto(), "No ganó la segunda fase");
        }
        check(game.getResultado().getDesenlace() == (eligible && accept
                ? Desenlace.VICTORIA_VERDADERA : Desenlace.VICTORIA), "Resultado final incorrecto");
        ResultadoPartida result = game.getResultado();
        rechaza(IllegalStateException.class, () -> game.arriesgar(caso.primerSecreto.getId()));
        rechaza(IllegalStateException.class, () -> game.decidirSegundaFase(false));
        check(game.getResultado() == result, "Se reemplazó el resultado final");
    }

    private static void checkFirstPhaseLoss(ModoJuego mode) {
        PartidaPrueba caso = new PartidaPrueba(mode, false);
        caso.fallarJugador();
        check(caso.partida.ejecutarTurnoMaquina().isAcierto(), "La máquina debía acertar");
        check(caso.partida.getResultado().getDesenlace() == Desenlace.DERROTA, "Derrota inicial incorrecta");
        check(caso.partida.getEstado() == EstadoPartida.FINALIZADA, "Derrota no finalizó");
        rechaza(IllegalStateException.class, () -> caso.partida.decidirSegundaFase(true));
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
