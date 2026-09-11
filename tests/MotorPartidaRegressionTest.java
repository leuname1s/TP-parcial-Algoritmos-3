import Funcionalidades.Partida;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;
import static soporte.PartidaPrueba.rechaza;

public class MotorPartidaRegressionTest {
    public static void main(String[] args) {
        InputStream entradaOriginal = System.in;
        PrintStream salidaOriginal = System.out;
        PrintStream errorOriginal = System.err;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (PrintStream captura = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setIn(new InputStream() {
                @Override public int read() { throw new AssertionError("El motor intentó leer la consola"); }
            });
            System.setOut(captura);
            System.setErr(captura);
            estadosYTurnos();
            consultasInmutables();
            turnosEspectador();
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
            System.setErr(errorOriginal);
        }
        comprobar(salida.size() == 0, "El motor escribió en consola");
        System.out.println("PASS: motor sin entrada/salida, estados, turnos, consultas inmutables y rechazos atómicos");
    }

    private static void estadosYTurnos() {
        Partida partida = new Partida(new Random(9));
        comprobar(partida.getEstado() == EstadoPartida.SIN_INICIAR && partida.getTurno() == null, "Estado inicial incorrecto");
        comprobar(partida.getPersonajes().isEmpty(), "Mazo antes de iniciar");
        rechaza(IllegalStateException.class, () -> partida.preguntar(Pregunta.USA_LENTES));
        rechaza(IllegalStateException.class, () -> partida.arriesgar(1));
        rechaza(IllegalStateException.class, partida::ejecutarTurnoMaquina);
        rechaza(IllegalStateException.class, partida::seleccionarPersonajeAleatorio);
        rechaza(IllegalStateException.class, () -> partida.decidirSegundaFase(true));
        rechaza(IllegalArgumentException.class, () -> partida.iniciar(null));
        rechaza(IllegalArgumentException.class, () -> partida.iniciar(ModoJuego.SALIR));
        comprobar(partida.getEstado() == EstadoPartida.SIN_INICIAR, "Modo inválido alteró estado");
        partida.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_1);
        rechaza(IllegalStateException.class, partida::ejecutarTurnoMaquina);
        rechaza(IllegalStateException.class, () -> partida.preguntar(Pregunta.USA_LENTES));
        rechaza(IllegalStateException.class, () -> partida.arriesgar(1));
        rechaza(IllegalStateException.class, () -> partida.decidirSegundaFase(false));
        comprobar(partida.getTurno() == null, "Turno antes de seleccionar personaje");
        partida.seleccionarPersonajeAleatorio();
        List<Personaje> mazo = partida.getPersonajes();
        Personaje secretoPropio = partida.getPersonajeJugador();
        rechaza(IllegalArgumentException.class, () -> partida.iniciar(ModoJuego.SALIR));
        comprobar(partida.getPersonajes().equals(mazo)
                && partida.getPersonajeJugador().equals(secretoPropio), "Modo inválido reinició la partida");
        rechaza(IllegalStateException.class, partida::seleccionarPersonajeAleatorio);
        rechaza(IllegalStateException.class, () -> partida.seleccionarPersonaje(secretoPropio.getId()));
        rechaza(IllegalStateException.class, partida::ejecutarTurnoMaquina);

        PartidaPrueba caso = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_2, true);
        caso.partida.preguntar(Pregunta.USA_LENTES);
        comprobar(caso.partida.getTurno() == Participante.MAQUINA_2, "No avanzó al rival");
        int ronda = caso.partida.getRonda();
        List<Personaje> candidatos = caso.partida.getCandidatos(Participante.JUGADOR);
        rechaza(IllegalStateException.class, () -> caso.partida.preguntar(Pregunta.TIENE_BARBA));
        rechaza(IllegalStateException.class, () -> caso.partida.arriesgar(caso.primerSecreto.getId()));
        comprobar(caso.partida.getCandidatos(Participante.JUGADOR).equals(candidatos)
                && caso.partida.getRonda() == ronda, "Acción fuera de turno modificó estado");
        caso.partida.ejecutarTurnoMaquina();
        comprobar(caso.partida.getTurno() == Participante.JUGADOR && caso.partida.getRonda() == ronda + 1,
                "No cerró la ronda");
    }

    private static void consultasInmutables() {
        PartidaPrueba caso = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_1, true);
        List<Personaje> mazo = caso.partida.getPersonajes();
        List<Personaje> candidatos = caso.partida.getCandidatos(Participante.JUGADOR);
        Set<Pregunta> preguntas = caso.partida.getPreguntasRealizadas();
        rechaza(UnsupportedOperationException.class, mazo::clear);
        rechaza(UnsupportedOperationException.class, candidatos::clear);
        rechaza(UnsupportedOperationException.class, () -> preguntas.add(Pregunta.USA_LENTES));
        caso.partida.preguntar(Pregunta.USA_LENTES);
        comprobar(candidatos.size() == 23 && preguntas.isEmpty(), "Consulta anterior cambió retroactivamente");
        comprobar(caso.partida.getPreguntasRealizadas().contains(Pregunta.USA_LENTES), "Falta pregunta actual");
    }

    private static void turnosEspectador() {
        Partida partida = new Partida(new Random(9), new PartidaPrueba.AzarFijo(0.99, 0),
                new PartidaPrueba.AzarFijo(0.99, 0));
        partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
        rechaza(IllegalStateException.class, partida::seleccionarPersonajeAleatorio);
        rechaza(IllegalStateException.class, () -> partida.preguntar(Pregunta.USA_LENTES));
        rechaza(IllegalStateException.class, () -> partida.arriesgar(1));
        rechaza(IllegalArgumentException.class, () -> partida.getSecretoEspectador(Participante.JUGADOR));
        comprobar(partida.ejecutarTurnoMaquina().getParticipante().equals("Máquina 1"), "No empezó Máquina 1");
        comprobar(partida.getTurno() == Participante.MAQUINA_2 && partida.getRonda() == 1, "Turno espectador incorrecto");
        comprobar(partida.ejecutarTurnoMaquina().getParticipante().equals("Máquina 2"), "No siguió Máquina 2");
        comprobar(partida.getTurno() == Participante.MAQUINA_1 && partida.getRonda() == 2, "Ronda espectador incorrecta");
    }
}
