import Funcionalidades.Partida;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoPartida;
import datos.ResultadoPartida.Desenlace;
import datos.ResultadoTurno;
import java.util.EnumSet;
import java.util.Random;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;
import static soporte.PartidaPrueba.rechaza;

public class ResultadosRegressionTest {
    public static void main(String[] args) {
        for (ModoJuego modo : new ModoJuego[] {ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2}) {
            PartidaPrueba caso = new PartidaPrueba(modo, true);
            caso.avanzarFallos(14);
            comprobar(caso.partida.getResultado() == null, "Partida incompleta con resultado");
            caso.partida.arriesgar(caso.primerSecreto.getId());
            comprobar(caso.partida.getResultado() == null, "Registró una victoria intermedia");
            caso.partida.decidirSegundaFase(true);
            while (caso.partida.getEstado() != EstadoPartida.FINALIZADA) {
                caso.fallarJugador();
                caso.partida.ejecutarTurnoMaquina();
            }
            comprobar(caso.partida.getResultado().getDesenlace() == Desenlace.DERROTA, "Perder fase 2 debe ser derrota");
            resultadoEstable(caso.partida);
        }

        EnumSet<Desenlace> ganadores = EnumSet.noneOf(Desenlace.class);
        for (int semilla = 0; semilla < 100 && ganadores.size() < 2; semilla++) {
            Partida partida = new Partida(new Random(semilla), new Random(semilla + 100), new Random(semilla + 200));
            partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
            terminarEspectador(partida);
            ResultadoPartida anterior = partida.getResultado();
            ganadores.add(anterior.getDesenlace());
            resultadoEstable(partida);
            partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
            comprobar(partida.getResultado() == null, "Reinicio conservó resultado");
            comprobar(partida.getFase() == 1 && partida.getRonda() == 1, "Reinicio conservó fase o ronda");
            terminarEspectador(partida);
            comprobar(!partida.getResultado().getId().equals(anterior.getId()), "Reinicio reutilizó ID");
        }
        comprobar(ganadores.equals(EnumSet.of(Desenlace.GANA_MAQUINA_1, Desenlace.GANA_MAQUINA_2)), "Falta un ganador");
        System.out.println("PASS: derrota en segunda fase, ambos ganadores, resultado estable y nuevo ID al reiniciar");
    }

    private static void terminarEspectador(Partida partida) {
        for (int turno = 0; turno < 46 && partida.getResultado() == null; turno++) {
            Participante participante = partida.getTurno();
            Personaje objetivo = partida.getSecretoEspectador(participante == Participante.MAQUINA_1
                    ? Participante.MAQUINA_2 : Participante.MAQUINA_1);
            ResultadoTurno accion = partida.ejecutarTurnoMaquina();
            if (accion.isAcierto()) {
                comprobar(accion.getIntento().equals(objetivo), "Ganó adivinando el secreto equivocado");
                comprobar(partida.getResultado().getDesenlace() == (participante == Participante.MAQUINA_1
                        ? Desenlace.GANA_MAQUINA_1 : Desenlace.GANA_MAQUINA_2), "Ganador registrado incorrecto");
            }
        }
        comprobar(partida.getResultado() != null, "Partida sin terminar");
    }

    private static void resultadoEstable(Partida partida) {
        ResultadoPartida resultado = partida.getResultado();
        rechaza(IllegalStateException.class, partida::ejecutarTurnoMaquina);
        rechaza(IllegalStateException.class, () -> partida.preguntar(Pregunta.USA_LENTES));
        rechaza(IllegalStateException.class, () -> partida.arriesgar(1));
        rechaza(IllegalStateException.class, () -> partida.decidirSegundaFase(true));
        comprobar(partida.getResultado() == resultado, "Acción posterior reemplazó el resultado");
    }
}
