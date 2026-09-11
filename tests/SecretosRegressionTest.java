import Funcionalidades.Maquina1;
import Funcionalidades.Maquina2;
import Funcionalidades.Partida;
import Funcionalidades.PartidaConsola;
import Funcionalidades.TableroCandidatos;
import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import datos.EstadoPartida;
import datos.MazoPersonajes;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoTurno;
import defaults.CatalogoPersonajes;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;
import static soporte.PartidaPrueba.rechaza;

public class SecretosRegressionTest {
    public static void main(String[] args) throws Exception {
        comprobar(IMaquina.class.getMethod("ejecutarTurno", IArbitroTurno.class) != null, "Falta el árbitro");
        for (Method method : IMaquina.class.getMethods()) {
            for (Class<?> parameter : method.getParameterTypes()) {
                comprobar(parameter != Personaje.class, "La API recibe el secreto");
            }
        }
        visibilidad();
        busquedas();
        segundaFase(ModoJuego.JUGADOR_VS_MAQUINA_1);
        segundaFase(ModoJuego.JUGADOR_VS_MAQUINA_2);
        espectadores();
        System.out.println("PASS: secretos protegidos, árbitro, 1440 búsquedas, ambas fases y 100 partidas espectador");
    }

    private static void visibilidad() {
        for (ModoJuego modo : new ModoJuego[] {ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2}) {
            Partida partida = new Partida(new Random(9));
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            new PartidaConsola(partida, new Scanner("2\n"),
                    new PrintStream(salida, true, StandardCharsets.UTF_8)).jugar(modo);
            String texto = salida.toString(StandardCharsets.UTF_8);
            comprobar(texto.contains("Tu personaje secreto:"), "No muestra el secreto propio");
            comprobar(!texto.contains("Personaje secreto de Máquina"), "La consola expone secretos rivales");
            for (Participante participante : Participante.values()) {
                rechaza(IllegalStateException.class, () -> partida.getSecretoEspectador(participante));
            }
        }
    }

    private static void busquedas() {
        MazoPersonajes mazo = CatalogoPersonajes.crearCatalogo();
        for (Personaje objetivo : mazo) {
            for (int semilla = 0; semilla < 10; semilla++) {
                for (int clase = 1; clase <= 2; clase++) {
                    for (boolean heredar : new boolean[] {false, true}) {
                        TableroCandidatos anterior = new TableroCandidatos(mazo);
                        if (heredar) {
                            anterior.descartarSegun(Pregunta.USA_LENTES, Pregunta.USA_LENTES.cumple(objetivo));
                        }
                        int cantidad = anterior.getCantidadViva();
                        IMaquina maquina = clase == 1 ? new Maquina1(mazo, anterior, new Random(semilla))
                                : new Maquina2(mazo, anterior, new Random(semilla));
                        IArbitroTurno arbitro = new IArbitroTurno() {
                            public boolean responder(Pregunta pregunta) { return pregunta.cumple(objetivo); }
                            public boolean comprobarIntento(int id) { return id == objetivo.getId(); }
                        };
                        boolean gano = false;
                        // Todo turno fallido debe eliminar al menos un candidato.
                        for (int turno = 0; turno < cantidad && !gano; turno++) {
                            int antes = maquina.getTablero().getCantidadViva();
                            ResultadoTurno accion = maquina.ejecutarTurno(arbitro);
                            gano = accion.isAcierto();
                            comprobar(maquina.getTablero().estaVivo(objetivo.getId()), "Descartó el secreto");
                            comprobar(gano || maquina.getTablero().getCantidadViva() < antes, "Turno sin progreso");
                            comprobar(accion.getCandidatosRestantes() == maquina.getTablero().getCantidadViva(),
                                    "Resultado de turno desactualizado");
                            if (gano) { comprobar(accion.getIntento().equals(objetivo), "Ganó con otro personaje"); }
                        }
                        comprobar(gano, "No encontró el secreto dentro del límite");
                        comprobar(anterior.getCantidadViva() == cantidad, "Modificó el tablero heredado original");
                    }
                }
            }
        }
    }

    private static void segundaFase(ModoJuego modo) {
        PartidaPrueba caso = new PartidaPrueba(modo, true);
        Partida partida = caso.partida;
        partida.preguntar(Pregunta.USA_LENTES);
        partida.ejecutarTurnoMaquina();
        Participante primera = partida.getRivalActual();
        List<Personaje> heredados = partida.getCandidatos(primera);
        partida.arriesgar(caso.primerSecreto.getId());
        partida.decidirSegundaFase(true);
        comprobar(partida.getPersonajeJugador().equals(caso.humano), "Cambió secreto humano");
        comprobar(!caso.primerSecreto.equals(caso.segundoSecreto), "Secretos repetidos");
        comprobar(partida.getCandidatos(Participante.JUGADOR).size() == 22, "Tablero humano incorrecto");
        for (Personaje personaje : caso.personajes) {
            comprobar(partida.getCandidatos(Participante.JUGADOR).contains(personaje)
                    == !personaje.equals(caso.primerSecreto), "Cambió mazo entre fases");
        }
        comprobar(partida.getCandidatos(partida.getRivalActual()).equals(heredados), "No heredó los candidatos");
        comprobar(partida.getPreguntasRealizadas().isEmpty(), "Preguntas no reiniciadas");
        rechaza(IllegalStateException.class, () -> partida.getSecretoEspectador(partida.getRivalActual()));
        ResultadoTurno pregunta = partida.preguntar(Pregunta.USA_LENTES);
        comprobar(pregunta.getRespuesta() == Pregunta.USA_LENTES.cumple(caso.segundoSecreto), "Usó el secreto anterior");
        partida.ejecutarTurnoMaquina();
        comprobar(partida.arriesgar(caso.segundoSecreto.getId()).isAcierto(), "No encontró el nuevo secreto");
    }

    private static void espectadores() {
        for (int semilla = 0; semilla < 100; semilla++) {
            Partida partida = new Partida(new Random(semilla), new Random(semilla), new Random(semilla + 100));
            partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
            comprobar(!partida.getSecretoEspectador(Participante.MAQUINA_1)
                    .equals(partida.getSecretoEspectador(Participante.MAQUINA_2)), "Secretos iguales");
            comprobar(partida.getCandidatos(Participante.MAQUINA_1)
                    .contains(partida.getSecretoEspectador(Participante.MAQUINA_2)), "Objetivo ausente");
            int turnos = 0;
            while (partida.getEstado() != EstadoPartida.FINALIZADA && turnos++ < 46) {
                partida.ejecutarTurnoMaquina();
            }
            comprobar(partida.getResultado() != null, "Partida espectador no finalizó");
        }
    }
}
