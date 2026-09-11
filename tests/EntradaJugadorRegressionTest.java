import Funcionalidades.Partida;
import Funcionalidades.PartidaConsola;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoTurno;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;
import static soporte.PartidaPrueba.rechaza;

public class EntradaJugadorRegressionTest {
    public static void main(String[] args) {
        preguntasRepetidasYFiltrado();
        intentosInvalidos();
        preguntasAgotadas();
        seleccionYReinicio();
        consolaInvalidaYReanudacion();
        consolaAmbasFases();
        System.out.println("PASS: API de entradas, preguntas repetidas y agotadas, ID inválidos, consola y reinicio");
    }

    private static void preguntasRepetidasYFiltrado() {
        PartidaPrueba caso = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_1, true);
        Partida partida = caso.partida;
        ResultadoTurno accion = partida.preguntar(Pregunta.ES_FEMENINO);
        comprobar(!accion.isAcierto(), "Preguntar no puede ganar");
        comprobar(accion.getRespuesta() == Pregunta.ES_FEMENINO.cumple(caso.primerSecreto), "Respuesta errónea");
        comprobar(partida.getCandidatos(Participante.JUGADOR).contains(caso.primerSecreto), "Se descartó el secreto");
        partida.ejecutarTurnoMaquina();
        List<Personaje> antes = partida.getCandidatos(Participante.JUGADOR);
        rechaza(IllegalArgumentException.class, () -> partida.preguntar(Pregunta.ES_FEMENINO));
        rechaza(IllegalArgumentException.class, () -> partida.preguntar(null));
        Personaje descartado = caso.personajes.stream().filter(p -> !antes.contains(p)).findFirst().orElseThrow();
        rechaza(IllegalArgumentException.class, () -> partida.arriesgar(descartado.getId()));
        comprobar(partida.getTurno() == Participante.JUGADOR && partida.getRonda() == 2, "Rechazo consumió turno");
        comprobar(partida.getCandidatos(Participante.JUGADOR).equals(antes), "Rechazo cambió candidatos");
        comprobar(partida.getPreguntasRealizadas().size() == 1, "Rechazo cambió historial");
        comprobar(partida.arriesgar(caso.primerSecreto.getId()).isAcierto(), "No se pudo ganar después del rechazo");
    }

    private static void intentosInvalidos() {
        PartidaPrueba caso = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_2, true);
        Partida partida = caso.partida;
        int fallido = caso.fallarJugador().getIntento().getId();
        partida.ejecutarTurnoMaquina();
        int ausente = 1;
        while (contieneId(caso.personajes, ausente)) { ausente++; }
        for (int id : new int[] {Integer.MIN_VALUE, 0, 37, Integer.MAX_VALUE, ausente, fallido}) {
            rechaza(IllegalArgumentException.class, () -> partida.arriesgar(id));
        }
        comprobar(partida.getCandidatos(Participante.JUGADOR).size() == 22, "Rechazo cambió candidatos");
        comprobar(partida.getTurno() == Participante.JUGADOR && partida.getRonda() == 2, "Rechazo cambió turno");
        comprobar(partida.arriesgar(caso.primerSecreto.getId()).isAcierto(), "Intento válido rechazado");
    }

    private static void preguntasAgotadas() {
        PartidaPrueba caso = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_1, true);
        for (Pregunta pregunta : Pregunta.values()) {
            comprobar(!caso.partida.preguntar(pregunta).isAcierto(), "Pregunta ganó el turno");
            caso.partida.ejecutarTurnoMaquina();
        }
        comprobar(caso.partida.getPreguntasRealizadas().size() == Pregunta.values().length, "Faltan preguntas");
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Scanner entrada = new Scanner("1\n3\n2\n" + caso.primerSecreto.getId() + "\n2\ncentinela\n");
        new PartidaConsola(caso.partida, entrada, new PrintStream(salida, true, StandardCharsets.UTF_8)).continuar();
        comprobar(salida.toString(StandardCharsets.UTF_8).contains("Ya realizaste todas las preguntas"), "Falta aviso");
        comprobar(caso.partida.getResultado() != null, "No se pudo salir del menú de preguntas agotadas");
        comprobar(entrada.nextLine().equals("centinela"), "La consola consumió entrada posterior al final");
    }

    private static void seleccionYReinicio() {
        Partida partida = new Partida(new Random(42));
        partida.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_1);
        int ausente = 1;
        while (contieneId(partida.getPersonajes(), ausente)) { ausente++; }
        final int idAusente = ausente;
        rechaza(IllegalArgumentException.class, () -> partida.seleccionarPersonaje(idAusente));
        comprobar(partida.getEstado() == EstadoPartida.SELECCION_PERSONAJE, "Selección inválida avanzó estado");
        comprobar(partida.getPersonajeJugador() == null, "Selección inválida asignó secreto");
        partida.seleccionarPersonaje(partida.getPersonajes().get(0).getId());
        partida.preguntar(Pregunta.USA_LENTES);
        partida.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_2);
        comprobar(partida.getPreguntasRealizadas().isEmpty(), "Reinicio conservó preguntas");
        comprobar(partida.getCandidatos(Participante.MAQUINA_1).isEmpty(), "Reinicio conservó rival");
        comprobar(partida.getResultado() == null && partida.getPersonajeJugador() == null, "Reinicio conservó datos");
        partida.seleccionarPersonajeAleatorio();
        comprobar(partida.getPersonajes().contains(partida.getPersonajeJugador()), "Secreto fuera del mazo");
        partida.preguntar(Pregunta.USA_LENTES);
    }

    private static void consolaInvalidaYReanudacion() {
        PartidaPrueba caso = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_2, true);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream impresora = new PrintStream(salida, true, StandardCharsets.UTF_8);
        new PartidaConsola(caso.partida, new Scanner("abc\n0\n3\n2\nabc\n0\n37\n"), impresora).continuar();
        comprobar(caso.partida.getTurno() == Participante.JUGADOR, "Entrada inválida consumió turno");
        comprobar(caso.partida.getCandidatos(Participante.JUGADOR).size() == 23, "Entrada inválida descartó");
        comprobar(caso.partida.getResultado() == null, "Fin de entrada inventó resultado");
        String texto = salida.toString(StandardCharsets.UTF_8);
        comprobar(texto.contains("Entrada no válida") && texto.contains("fuera de rango")
                && texto.contains("no pertenece"), "Faltan mensajes de entrada inválida");
        new PartidaConsola(caso.partida,
                new Scanner("2\n" + caso.primerSecreto.getId() + "\n2\n"), impresora).continuar();
        comprobar(caso.partida.getResultado() != null, "No se pudo reanudar la consola");

        Partida seleccion = new Partida(new Random(42));
        seleccion.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_1);
        int id = seleccion.getPersonajes().get(0).getId();
        new PartidaConsola(seleccion, new Scanner("1\nabc\n0\n37\n" + id + "\n"), impresora).continuar();
        comprobar(seleccion.getPersonajeJugador().getId() == id, "Selección manual por consola incorrecta");

        PartidaPrueba repetida = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_1, true);
        salida.reset();
        new PartidaConsola(repetida.partida, new Scanner("1\n1\n1\n1\n8\n"), impresora).continuar();
        comprobar(salida.toString(StandardCharsets.UTF_8).contains("Ya realizaste esa pregunta"), "Falta aviso de repetición");
        comprobar(repetida.partida.getPreguntasRealizadas().size() == 2
                && repetida.partida.getRonda() == 3, "La repetición consumió un turno");
    }

    private static boolean contieneId(List<Personaje> personajes, int id) {
        return personajes.stream().anyMatch(p -> p.getId() == id);
    }

    private static void consolaAmbasFases() {
        for (ModoJuego modo : new ModoJuego[] {ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2}) {
            PartidaPrueba esperado = new PartidaPrueba(modo, true);
            Partida partida = new Partida(new Random(9), new PartidaPrueba.AzarFijo(0.0, 0),
                    new PartidaPrueba.AzarFijo(0.0, 0));
            Scanner entrada = new Scanner("1\n" + esperado.humano.getId() + "\n2\n"
                    + esperado.primerSecreto.getId() + "\n0\nabc\n1\n2\n"
                    + esperado.segundoSecreto.getId() + "\ncentinela\n");
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            new PartidaConsola(partida, entrada,
                    new PrintStream(salida, true, StandardCharsets.UTF_8)).jugar(modo);
            String texto = salida.toString(StandardCharsets.UTF_8);
            comprobar(texto.contains("INICIANDO FASE 2") && texto.contains("Victoria verdadera"),
                    "La consola no completó ambas fases");
            comprobar(partida.getResultado().getDesenlace() == datos.ResultadoPartida.Desenlace.VICTORIA_VERDADERA,
                    "Resultado de consola incorrecto");
            comprobar(entrada.nextLine().equals("centinela"), "La consola consumió entrada después del final");
        }
    }
}
