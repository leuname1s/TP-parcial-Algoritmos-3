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

    private int indicePelo = 0;
    private boolean colorEncontrado = false;
    private boolean preguntoLentes = false;

    // Solo se necesita preguntar por Amarillo y Negro. Si ambas son NO, por descarte el pelo es Colorado.
    private static final Pregunta[] PREGUNTAS_PELO = {
            Pregunta.PELO_AMARILLO,
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

        // FASE 1: Averiguar color de pelo (preguntando únicamente por amarillo y luego negro)
        if (!colorEncontrado && indicePelo < PREGUNTAS_PELO.length) {
            Pregunta preguntaPelo = PREGUNTAS_PELO[indicePelo++];
            boolean respuesta = preguntaPelo.cumple(objetivoEnemigo);

            System.out.println("[" + nombre + "] Pregunta: \"" + preguntaPelo + "\"");
            System.out.println("[" + nombre + "] Respuesta recibida: " + (respuesta ? "SÍ" : "NO"));

            int descartados = tablero.descartarSegun(preguntaPelo, respuesta);
            System.out.println("[" + nombre + "] Descartó " + descartados
                    + " candidatos. Le quedan " + tablero.getCantidadViva() + " candidatos.");

            if (respuesta) {
                colorEncontrado = true; // Si es SÍ, se descubrió el color y salta el resto de preguntas de pelo
            } else if (indicePelo == PREGUNTAS_PELO.length) {
                // Al recibir el 2do NO, deduce por descarte que el pelo debe ser colorado y omite la pregunta
                System.out.println("[" + nombre + "] (Deducción lógica: Al recibir 2 'NO', el pelo es colorado. Omite preguntar por colorado).");
                colorEncontrado = true;
            }

            return false;
        }

        // FASE 2: Una vez determinado el color de pelo, pregunta si usa lentes
        if (!preguntoLentes) {
            preguntoLentes = true;
            Pregunta preguntaLentes = Pregunta.USA_LENTES;
            boolean respuesta = preguntaLentes.cumple(objetivoEnemigo);

            System.out.println("[" + nombre + "] Pregunta: \"" + preguntaLentes + "\"");
            System.out.println("[" + nombre + "] Respuesta recibida: " + (respuesta ? "SÍ" : "NO"));

            int descartados = tablero.descartarSegun(preguntaLentes, respuesta);
            System.out.println("[" + nombre + "] Descartó " + descartados
                    + " candidatos. Le quedan " + tablero.getCantidadViva() + " candidatos.");

            return false;
        }

        // FASE 3: Intenta adivinar aleatoriamente entre los personajes vivos restantes
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
