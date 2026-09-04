package funcionalidades;

import datos.Personaje;
import datos.Pregunta;
import interfaces.IMaquina;
import interfaces.ITableroCandidatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maquina2 implements IMaquina {

    private final String nombre = "Máquina 2";
    private final TableroCandidatos tablero;
    private final MazoPersonajes mazo;
    private final Random random;

    private int pasoPregunta = 0;
    private static final Pregunta[] SECUENCIA_PREGUNTAS = {
            Pregunta.ES_FEMENINO,
            Pregunta.USA_LENTES,
            Pregunta.ES_CALVO,
            Pregunta.PELO_NEGRO
    };

    public Maquina2(MazoPersonajes mazo) {
        this.mazo = mazo;
        this.tablero = new TableroCandidatos(mazo);
        this.random = new Random();
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public ITableroCandidatos getTablero() {
        return tablero;
    }

    @Override
    public boolean ejecutarTurno(Personaje objetivoEnemigo) {
        System.out.println("\n--- TURNO DE " + nombre.toUpperCase() + " ---");

        if (pasoPregunta < SECUENCIA_PREGUNTAS.length) {
            Pregunta preguntaActual = SECUENCIA_PREGUNTAS[pasoPregunta];
            pasoPregunta++;

            boolean respuesta = preguntaActual.cumple(objetivoEnemigo);
            System.out.println("[" + nombre + "] Pregunta: \"" + preguntaActual + "\"");
            System.out.println("[" + nombre + "] Respuesta recibida: " + (respuesta ? "SÍ" : "NO"));

            int descartados = tablero.descartarSegun(preguntaActual, respuesta);
            System.out.println("[" + nombre + "] Descartó " + descartados
                    + " candidatos. Le quedan " + tablero.getCantidadViva() + " candidatos.");

            Personaje unico = tablero.unicoSobreviviente();
            if (unico != null) {
                System.out.println("[" + nombre + "] Solo le queda 1 candidato en su tablero: " + unico.getNombre());
                if (unico.getId() == objetivoEnemigo.getId()) {
                    System.out.println("\n**************************************************");
                    System.out.println("   ¡" + nombre.toUpperCase() + " HA ADIVINADO EL PERSONAJE!   ");
                    System.out.println("   El personaje era: " + unico.getNombre() + " (ID: " + unico.getId() + ")");
                    System.out.println("**************************************************\n");
                    return true;
                }
            }

            return false;
        }

        List<Personaje> vivos = new ArrayList<>();
        for (Personaje p : mazo) {
            if (tablero.estaVivo(p.getId())) {
                vivos.add(p);
            }
        }

        if (vivos.isEmpty()) {
            System.out.println("[" + nombre + "] No le quedan candidatos vivos.");
            return false;
        }

        Personaje candidatoElegido = vivos.get(random.nextInt(vivos.size()));
        System.out.println("[" + nombre + "] Arriesga adivinando aleatoriamente por: "
                + candidatoElegido.getNombre() + " (ID: " + candidatoElegido.getId() + ")");

        if (candidatoElegido.getId() == objetivoEnemigo.getId()) {
            System.out.println("\n**************************************************");
            System.out.println("   ¡" + nombre.toUpperCase() + " HA ADIVINADO EL PERSONAJE!   ");
            System.out.println("   El personaje era: " + candidatoElegido.getNombre() + " (ID: " + candidatoElegido.getId() + ")");
            System.out.println("**************************************************\n");
            return true;
        } else {
            System.out.println("[" + nombre + "] Falló la adivinanza.");
            tablero.descartar(candidatoElegido.getId());
            System.out.println("[" + nombre + "] Descartó a " + candidatoElegido.getNombre()
                    + " de su tablero. Le quedan " + tablero.getCantidadViva() + " candidatos.");
            return false;
        }
    }
}
