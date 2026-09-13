import Funcionalidades.Partida;
import Interfaces.IPartida;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoPartida;
import datos.ResultadoPartida.Desenlace;
import java.util.List;
import java.util.Random;
import java.util.Set;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;
import static soporte.PartidaPrueba.rechaza;

public class RevelacionFinalRegressionTest {
    public static void main(String[] args) {
        IPartida sinIniciar = new Partida(new Random(9));
        revelacionBloqueada(sinIniciar);
        for (ModoJuego modo : new ModoJuego[] {
                ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2}) {
            seleccionYRechazo(modo);
            victoriaSinSegundaFase(modo);
            derrotaPrimeraFase(modo);
            segundaFase(modo, true);
            segundaFase(modo, false);
        }
        modoEspectador();
        System.out.println("PASS: secreto final humano, bloqueo previo, ambos órdenes de fase, derrota, reinicio y espectador");
    }

    private static void seleccionYRechazo(ModoJuego modo) {
        IPartida seleccion = new Partida(new Random(9));
        seleccion.iniciar(modo);
        comprobar(seleccion.getEstado() == EstadoPartida.SELECCION_PERSONAJE, "Falta selección inicial");
        revelacionBloqueada(seleccion);
        seleccion.seleccionarPersonajeAleatorio();
        revelacionBloqueada(seleccion);

        PartidaPrueba caso = new PartidaPrueba(modo, true);
        IPartida partida = caso.partida;
        revelacionBloqueada(partida);
        caso.fallarJugador();
        revelacionBloqueada(partida);
        comprobar(!partida.ejecutarTurnoMaquina().isAcierto(), "La máquina debía fallar");
        revelacionBloqueada(partida);
        comprobar(partida.arriesgar(caso.primerSecreto.getId()).isAcierto(), "El humano debía acertar");
        comprobar(partida.getEstado() == EstadoPartida.DECISION_SEGUNDA_FASE, "Falta decisión de fase");
        revelacionBloqueada(partida);
        partida.decidirSegundaFase(false);
        revelacionFinal(partida, caso.primerSecreto, Desenlace.VICTORIA);

        partida.iniciar(modo);
        comprobar(partida.getEstado() == EstadoPartida.SELECCION_PERSONAJE, "Reinicio fuera de selección");
        revelacionBloqueada(partida);
        partida.seleccionarPersonajeAleatorio();
        revelacionBloqueada(partida);
    }

    private static void victoriaSinSegundaFase(ModoJuego modo) {
        PartidaPrueba caso = new PartidaPrueba(modo, true);
        caso.avanzarFallos(15);
        revelacionBloqueada(caso.partida);
        comprobar(caso.partida.arriesgar(caso.primerSecreto.getId()).isAcierto(), "El humano debía ganar");
        revelacionFinal(caso.partida, caso.primerSecreto, Desenlace.VICTORIA);
    }

    private static void derrotaPrimeraFase(ModoJuego modo) {
        PartidaPrueba caso = new PartidaPrueba(modo, false);
        caso.fallarJugador();
        revelacionBloqueada(caso.partida);
        comprobar(caso.partida.ejecutarTurnoMaquina().isAcierto(), "La máquina debía ganar");
        revelacionFinal(caso.partida, caso.primerSecreto, Desenlace.DERROTA);
    }

    private static void segundaFase(ModoJuego modo, boolean ganaJugador) {
        PartidaPrueba caso = new PartidaPrueba(modo, ganaJugador);
        IPartida partida = caso.partida;
        Participante primerRival = partida.getRivalActual();
        comprobar(partida.arriesgar(caso.primerSecreto.getId()).isAcierto(), "El humano debía ganar fase 1");
        revelacionBloqueada(partida);
        partida.decidirSegundaFase(true);
        comprobar(partida.getFase() == 2 && partida.getRivalActual() != primerRival, "No cambió de rival en fase 2");
        comprobar(!caso.primerSecreto.equals(caso.segundoSecreto), "El escenario necesita secretos diferentes");
        revelacionBloqueada(partida);
        caso.fallarJugador();
        revelacionBloqueada(partida);
        comprobar(partida.ejecutarTurnoMaquina().isAcierto() == !ganaJugador, "Desenlace de máquina inesperado");
        if (ganaJugador) {
            revelacionBloqueada(partida);
            comprobar(partida.arriesgar(caso.segundoSecreto.getId()).isAcierto(), "El humano debía ganar fase 2");
        }
        revelacionFinal(partida, caso.segundoSecreto,
                ganaJugador ? Desenlace.VICTORIA_VERDADERA : Desenlace.DERROTA);
        comprobar(!partida.getSecretoRivalFinal().equals(caso.primerSecreto), "Reveló al rival de fase 1");
        partida.iniciar(modo);
        revelacionBloqueada(partida);
    }

    private static void modoEspectador() {
        IPartida partida = new Partida(new Random(9), new Random(10), new Random(11));
        partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
        Personaje primero = partida.getSecretoEspectador(Participante.MAQUINA_1);
        Personaje segundo = partida.getSecretoEspectador(Participante.MAQUINA_2);
        comprobar(primero != null && segundo != null && !primero.equals(segundo), "Secretos espectador inválidos");
        rechaza(IllegalArgumentException.class, () -> partida.getSecretoEspectador(null));
        rechaza(IllegalArgumentException.class, () -> partida.getSecretoEspectador(Participante.JUGADOR));
        for (int turno = 0; turno < 46 && partida.getEstado() != EstadoPartida.FINALIZADA; turno++) {
            revelacionBloqueada(partida);
            partida.ejecutarTurnoMaquina();
        }
        comprobar(partida.getEstado() == EstadoPartida.FINALIZADA, "El espectador no finalizó");
        revelacionBloqueada(partida);
        comprobar(partida.getSecretoEspectador(Participante.MAQUINA_1).equals(primero)
                && partida.getSecretoEspectador(Participante.MAQUINA_2).equals(segundo), "Cambió la consulta espectador");
        partida.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_1);
        revelacionBloqueada(partida);
        secretosEspectadorBloqueados(partida);
    }

    private static void revelacionBloqueada(IPartida partida) {
        EstadoPartida estado = partida.getEstado();
        Participante turno = partida.getTurno();
        int ronda = partida.getRonda();
        ResultadoPartida resultado = partida.getResultado();
        List<Personaje> candidatos = partida.getCandidatos(Participante.JUGADOR);
        Set<Pregunta> preguntas = partida.getPreguntasRealizadas();
        rechaza(IllegalStateException.class, partida::getSecretoRivalFinal);
        comprobar(partida.getEstado() == estado && partida.getTurno() == turno && partida.getRonda() == ronda
                && partida.getResultado() == resultado && partida.getCandidatos(Participante.JUGADOR).equals(candidatos)
                && partida.getPreguntasRealizadas().equals(preguntas), "La consulta rechazada alteró la partida");
        if (partida.getModo() != ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            secretosEspectadorBloqueados(partida);
        }
    }

    private static void revelacionFinal(IPartida partida, Personaje esperado, Desenlace desenlace) {
        comprobar(partida.getEstado() == EstadoPartida.FINALIZADA, "La partida debía estar finalizada");
        ResultadoPartida resultado = partida.getResultado();
        comprobar(resultado.getDesenlace() == desenlace, "Desenlace inesperado");
        Personaje revelado = partida.getSecretoRivalFinal();
        comprobar(revelado.equals(esperado), "Reveló un secreto rival incorrecto");
        comprobar(partida.getSecretoRivalFinal() == revelado && partida.getResultado() == resultado,
                "La consulta final cambió el secreto o resultado");
        secretosEspectadorBloqueados(partida);
    }

    private static void secretosEspectadorBloqueados(IPartida partida) {
        for (Participante participante : Participante.values()) {
            rechaza(IllegalStateException.class, () -> partida.getSecretoEspectador(participante));
        }
    }
}
