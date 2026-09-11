package Funcionalidades;

import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import datos.DiagnosticoTurno;
import datos.DiagnosticoTurno.Comparacion;
import datos.DiagnosticoTurno.Motivo;
import datos.ResultadoTurno;
import datos.ColorPelo;
import datos.Genero;
import datos.MazoPersonajes;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import static soporte.PartidaPrueba.rechaza;

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

    private static void checkDecisionLog() {
        MazoPersonajes deck = new MazoPersonajes();
        Personaje target = new Personaje(2, "Secret", Genero.FEMENINO, ColorPelo.NEGRO, false, false, false);
        deck.agregar(target);
        deck.agregar(new Personaje(7, "B", Genero.FEMENINO, ColorPelo.NEGRO, true, false, false));
        deck.agregar(new Personaje(18, "C", Genero.FEMENINO, ColorPelo.PELIRROJO, true, false, false));
        deck.agregar(new Personaje(36, "D", Genero.MASCULINO, ColorPelo.PELIRROJO, false, false, false));
        for (int kind = 1; kind <= 2; kind++) {
            for (boolean risk : new boolean[] {false, true}) {
                int[] randomCalls = {0};
                Random random = new Random(0) {
                    private static final long serialVersionUID = 1L;
                    @Override public double nextDouble() { randomCalls[0]++; return risk ? 0.0 : 0.99; }
                    @Override public int nextInt(int bound) { randomCalls[0]++; return 0; }
                };
                TableroCandidatos board = new TableroCandidatos(deck);
                IMaquina machine = kind == 1 ? new Maquina1(deck, board, random) : new Maquina2(deck, board, random);
                ResultadoTurno action = machine.ejecutarTurno(new IArbitroTurno() {
                    @Override public boolean responder(Pregunta question) { return question.cumple(target); }
                    @Override public boolean comprobarIntento(int id) { return id == target.getId(); }
                });
                DiagnosticoTurno data = action.getDiagnostico();
                check(data.getCandidatosAntes() == 4 && data.getDescartadosAntes() == 0, "Wrong initial counts");
                check(data.getPorcentajeRiesgo() == 4.0 && data.getIncremento() == (kind == 1 ? 5.0 : 2.5),
                        "Wrong risk data");
                check(data.getSorteo() == (risk ? 0.0 : 0.99), "Wrong risk draw");
                check(data.getMotivo() == (risk ? Motivo.SORTEO_RIESGO : Motivo.PREGUNTA), "Wrong motive");
                rechaza(UnsupportedOperationException.class, () -> data.getComparaciones().clear());
                rechaza(UnsupportedOperationException.class, () -> data.getMejoresPreguntas().clear());
                if (!risk) {
                    check(data.getComparaciones().size() == Pregunta.values().length, "Missing comparisons");
                    check(data.getMejoresPreguntas().contains(action.getPregunta()), "Chosen question not among ties");
                    check(action.getIntento() == null, "Question exposed a secret");
                    for (Comparacion comparison : data.getComparaciones()) {
                        int yes = board.contarSiCumplen(comparison.getPregunta());
                        check(comparison.getCantidadSi() == yes && comparison.getCantidadNo() == 4 - yes,
                                "Comparison used the filtered board");
                    }
                } else {
                    check(data.getComparaciones().isEmpty(), "Risk turn compared questions");
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                PartidaConsola console = new PartidaConsola(new Partida(), new Scanner(""),
                        new PrintStream(output, true, StandardCharsets.UTF_8));
                int callsBefore = randomCalls[0];
                console.mostrarTurno(action);
                check(randomCalls[0] == callsBefore, "Presentation reran the random decision");
                String log = output.toString(StandardCharsets.UTF_8);
                check(log.contains("[Máquina " + kind + "] Cálculo: 4% +"), "Missing identity or risk explanation");
                if (risk) {
                    check(log.contains("El sorteo indica arriesgar")
                            && log.contains("cada uno tiene 1 posibilidad entre 4"), "Missing risk explanation");
                    check(!log.contains("Comparo todas"), "Risk turn compared questions");
                } else {
                    check(log.contains("El sorteo no indica arriesgar")
                            && log.contains(kind == 1 ? "Busco la mayor" : "Busco la menor"), "Wrong strategy explanation");
                    check(log.contains("Desempato al azar") && log.contains("La ignoro:")
                            && log.contains("Si responde SÍ, descarto") && log.contains("si responde NO, descarto"),
                            "Missing tie, constant question or possible outcomes");
                    check(!log.contains(target.getNombre()), "Question log leaked the secret");
                }
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
