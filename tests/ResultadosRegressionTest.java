import Funcionalidades.Partida;
import Interfaces.*;
import datos.*;
import datos.ResultadoPartida.Desenlace;
import defaults.CatalogoPersonajes;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ResultadosRegressionTest {
    public static void main(String[] args) throws Exception {
        PrintStream original = System.out;
        try (PrintStream silencio = new PrintStream(OutputStream.nullOutputStream())) {
            System.setOut(silencio);
            for (ModoJuego modo : new ModoJuego[] {ModoJuego.JUGADOR_VS_MAQUINA_1, ModoJuego.JUGADOR_VS_MAQUINA_2}) {
                Random random = new Random(9);
                MazoPersonajes mazo = CatalogoPersonajes.crearMazo(random);
                List<Personaje> lista = new ArrayList<>();
                for (Personaje p : mazo) { lista.add(p); }
                Personaje primero = lista.get(random.nextInt(lista.size()));
                lista.remove(primero);
                Personaje segundo = lista.get(random.nextInt(lista.size()));
                Personaje humano = mazo.iterator().next();
                StringBuilder entrada = new StringBuilder("1\n" + humano.getId() + "\n2\n" + primero.getId() + "\n1\n");
                for (Personaje p : mazo) {
                    if (p != primero && p != segundo) { entrada.append("2\n").append(p.getId()).append('\n'); }
                }
                Partida partida = new Partida(new Scanner(entrada.toString()), new Random(9));
                partida.iniciar(modo);
                check(partida.getResultado() == null, "Partida incompleta con resultado");
                ITableroCandidatos tablero = modo == ModoJuego.JUGADOR_VS_MAQUINA_1
                        ? partida.getTableroMaquina1() : partida.getTableroMaquina2();
                int descartados = 0;
                for (Personaje p : mazo) {
                    if (p != humano && descartados < 14) { tablero.descartar(p.getId()); descartados++; }
                }
                partida.jugar();
                check(partida.getResultado().getDesenlace() == Desenlace.DERROTA, "Perder fase 2 debe ser derrota");
            }
            for (int ganador = 1; ganador <= 2; ganador++) {
                Partida partida = new Partida(new Scanner(""), new Random(9));
                partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
                for (int numero = 1; numero <= 2; numero++) {
                    final boolean gana = numero == ganador;
                    IMaquina maquina = new IMaquina() {
                        public String getNombre() { return "Prueba"; }
                        public ITableroCandidatos getTablero() { return null; }
                        public boolean ejecutarTurno(IArbitroTurno arbitro) { return gana; }
                    };
                    Field campo = Partida.class.getDeclaredField("maquina" + numero);
                    campo.setAccessible(true);
                    campo.set(partida, maquina);
                }
                partida.jugar();
                ResultadoPartida resultado = partida.getResultado();
                check(resultado.getDesenlace() == (ganador == 1 ? Desenlace.GANA_MAQUINA_1 : Desenlace.GANA_MAQUINA_2),
                        "Ganador de máquinas incorrecto");
                partida.iniciar(ModoJuego.MAQUINA_1_VS_MAQUINA_2);
                check(partida.getResultado() == null, "Reinicio conservó el resultado");
                partida.jugar();
                check(!partida.getResultado().getId().equals(resultado.getId()), "Reinicio reutilizó ID");
            }
        } finally { System.setOut(original); }
        System.out.println("PASS: derrota en segunda fase, ganadores de máquinas y reinicio");
    }
    private static void check(boolean valor, String mensaje) { if (!valor) { throw new AssertionError(mensaje); } }
}
