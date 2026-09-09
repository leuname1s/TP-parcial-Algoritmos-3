package Funcionalidades;

import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import datos.ColorPelo;
import datos.Genero;
import datos.MazoPersonajes;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;

public class DiagnosticoMaquinasRegressionTest {
    public static void main(String[] args) throws Exception {
        checkBalancedSelection();
        checkDecisionLog();
        System.out.println("PASS: optimal questions on 22000 sampled boards, including blond 5/18 cases; "
                + "question comparison, ties and risk explanations for both machines");
    }

    private static void checkBalancedSelection() {
        int blondCases = 0;
        for (int seed = 0; seed < 1000; seed++) {
            MazoPersonajes deck = CatalogoPersonajes.crearMazo(new Random(seed));
            TableroCandidatos board = new TableroCandidatos(deck);
            Random choices = new Random(seed);
            for (Personaje discarded : deck) {
                if (board.getCantidadViva() == 1) { break; }
                Pregunta chosen = EstrategiaMaquina.EQUILIBRADA.seleccionarPregunta(board, choices);
                int size = board.getCantidadViva();
                int yes = board.contarSiCumplen(chosen);
                int guaranteedDiscard = Math.min(yes, size - yes);
                check(guaranteedDiscard > 0, "Machine 2 chose a constant question");
                for (Pregunta question : Pregunta.values()) {
                    int matching = 0;
                    for (Personaje candidate : deck) {
                        if (board.estaVivo(candidate.getId()) && question.cumple(candidate)) { matching++; }
                    }
                    check(guaranteedDiscard >= Math.min(matching, size - matching),
                            "A more balanced question exists, seed " + seed + ", candidates " + size);
                }
                if (size == 23 && board.contarSiCumplen(Pregunta.PELO_RUBIO) == 5) {
                    blondCases++;
                    check(chosen != Pregunta.PELO_RUBIO, "Selected blond 5/18 over a balanced alternative");
                }
                board.descartar(discarded.getId());
            }
        }
        check(blondCases > 0, "No blond 5/18 case tested");
    }

    private static void checkDecisionLog() throws Exception {
        MazoPersonajes deck = new MazoPersonajes();
        Personaje target = new Personaje(2, "Secret", Genero.FEMENINO, ColorPelo.NEGRO, false, false, false);
        deck.agregar(target);
        deck.agregar(new Personaje(7, "B", Genero.FEMENINO, ColorPelo.NEGRO, true, false, false));
        deck.agregar(new Personaje(18, "C", Genero.FEMENINO, ColorPelo.PELIRROJO, true, false, false));
        deck.agregar(new Personaje(36, "D", Genero.MASCULINO, ColorPelo.PELIRROJO, false, false, false));
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            for (int kind = 1; kind <= 2; kind++) {
                for (boolean risk : new boolean[] {false, true}) {
                    output.reset();
                    Random random = new Random(0) {
                        private static final long serialVersionUID = 1L;
                        @Override public double nextDouble() { return risk ? 0.0 : 0.99; }
                        @Override public int nextInt(int bound) { return 0; }
                    };
                    TableroCandidatos board = new TableroCandidatos(deck);
                    IMaquina machine = kind == 1 ? new Maquina1(deck, board, random)
                            : new Maquina2(deck, board, random);
                    machine.ejecutarTurno(new IArbitroTurno() {
                        @Override public boolean responder(Pregunta question) {
                            String beforeAnswer = output.toString(java.nio.charset.StandardCharsets.UTF_8);
                            check(!beforeAnswer.contains(target.getNombre()), "Log leaked the secret before the answer");
                            for (Pregunta compared : Pregunta.values()) {
                                check(beforeAnswer.contains(compared + " -> sí:"),
                                        "Missing comparison before querying the arbiter");
                            }
                            return question.cumple(target);
                        }
                        @Override public boolean comprobarIntento(int id) { return id == target.getId(); }
                    });
                    String log = output.toString("UTF-8");
                    check(log.contains("[Máquina " + kind + "] Cálculo: 4% +"),
                            "Missing machine identity or risk explanation");
                    if (risk) {
                        check(log.contains("El sorteo indica arriesgar")
                                && log.contains("cada uno tiene 1 posibilidad entre 4"),
                                "Missing risk or candidate-selection explanation");
                        check(!log.contains("Comparo todas"), "Risk turn compared questions");
                    } else {
                        check(log.contains("El sorteo no indica arriesgar")
                                && log.contains(kind == 1 ? "Busco la mayor" : "Busco la menor"),
                                "Missing decision or wrong strategy explanation");
                        check(log.contains("Desempato al azar") && log.contains("La ignoro:")
                                && log.contains("Si responde SÍ, descarto")
                                && log.contains("si responde NO, descarto"),
                                "Missing tie, constant-question or possible-outcome explanation");
                    }
                }
            }
        } finally {
            System.setOut(original);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
