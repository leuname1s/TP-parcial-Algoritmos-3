package Funcionalidades;

import Interfaces.IArbitroTurno;
import datos.MazoPersonajes;
import datos.Personaje;
import datos.Pregunta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

enum EstrategiaMaquina {
    AGRESIVA(5.0, false),
    EQUILIBRADA(2.5, true);

    private final double incrementoPorDescarte;
    private final boolean buscaEquilibrio;

    EstrategiaMaquina(double incrementoPorDescarte, boolean buscaEquilibrio) {
        this.incrementoPorDescarte = incrementoPorDescarte;
        this.buscaEquilibrio = buscaEquilibrio;
    }

    double porcentajeRiesgo(int descartados) {
        return Math.min(100.0, 4.0 + incrementoPorDescarte * descartados);
    }

    Pregunta seleccionarPregunta(TableroCandidatos tablero, Random random) {
        return seleccionarPregunta(tablero, random, mensaje -> {});
    }

    // Compara la división inmediata de candidatos: minimiza o maximiza la diferencia
    // según la estrategia y reúne todos los empates para elegir sin favorecer el orden.
    private Pregunta seleccionarPregunta(TableroCandidatos tablero, Random random, Consumer<String> registrar) {
        List<Pregunta> mejores = new ArrayList<>();
        int mejorDiferencia = buscaEquilibrio ? Integer.MAX_VALUE : -1;
        int cantidad = tablero.getCantidadViva();
        registrar.accept("Comparo todas las preguntas sobre mis " + cantidad + " candidatos vivos."
                + " Busco la " + (buscaEquilibrio ? "menor" : "mayor")
                + " diferencia entre sí y no; ignoro las preguntas constantes.");
        for (Pregunta pregunta : Pregunta.values()) {
            int cantidadSi = tablero.contarSiCumplen(pregunta);
            int cantidadNo = cantidad - cantidadSi;
            if (cantidadSi == 0 || cantidadSi == cantidad) {
                registrar.accept("  " + pregunta + " -> sí: " + cantidadSi + ", no: " + cantidadNo
                        + ". La ignoro: todos responderían lo mismo.");
                continue;
            }
            int diferencia = Math.abs(cantidadSi - cantidadNo);
            registrar.accept("  " + pregunta + " -> sí: " + cantidadSi + ", no: " + cantidadNo
                    + ", diferencia: " + diferencia + ".");
            if (buscaEquilibrio ? diferencia < mejorDiferencia : diferencia > mejorDiferencia) {
                mejores.clear();
                mejorDiferencia = diferencia;
            }
            if (diferencia == mejorDiferencia) {
                mejores.add(pregunta);
            }
        }
        if (mejores.isEmpty()) {
            return null;
        }
        Pregunta elegida = mejores.get(random.nextInt(mejores.size()));
        registrar.accept("Mejor diferencia: " + mejorDiferencia + ". Preguntas con ese valor: " + mejores + ".");
        registrar.accept(mejores.size() > 1
                ? "Desempato al azar entre " + mejores.size() + " preguntas: " + elegida
                : "Hay una única mejor pregunta: " + elegida);
        return elegida;
    }

    boolean ejecutarTurno(String nombre, MazoPersonajes mazo, TableroCandidatos tablero,
            Random random, IArbitroTurno arbitro) {
        System.out.println("\n--- TURNO DE " + nombre.toUpperCase() + " ---");
        int cantidad = tablero.getCantidadViva();
        if (cantidad == 0) {
            throw new IllegalStateException("La máquina no tiene candidatos para continuar");
        }

        // Comparar con el mazo original incluye los descartes heredados en el riesgo.
        int descartados = mazo.getCantidad() - cantidad;
        double porcentaje = porcentajeRiesgo(descartados);
        System.out.println("[" + nombre + "] Candidatos: " + cantidad + "; descartados: "
                + descartados + "; probabilidad de arriesgar: " + porcentaje + "%.");
        System.out.println("[" + nombre + "] Cálculo: 4% + " + incrementoPorDescarte
                + " puntos por cada uno de los " + descartados + " descartes (incluidos los heredados),"
                + " con un máximo de 100%.");

        if (cantidad == 1) {
            System.out.println("[" + nombre + "] Queda un único candidato: intento obligatorio.");
            return arriesgar(nombre, tablero, tablero.unicoSobreviviente(), arbitro);
        }

        double sorteo = random.nextDouble();
        System.out.println("[" + nombre + "] Sorteo de riesgo: " + (sorteo * 100.0)
                + "; arriesga si es menor que " + porcentaje + ".");
        if (sorteo >= porcentaje / 100.0) {
            System.out.println("[" + nombre + "] El sorteo no indica arriesgar: busco una pregunta.");
            Pregunta pregunta = seleccionarPregunta(tablero, random,
                    mensaje -> System.out.println("[" + nombre + "] " + mensaje));
            if (pregunta != null) {
                int cantidadSi = tablero.contarSiCumplen(pregunta);
                System.out.println("[" + nombre + "] Busca la división más "
                        + (buscaEquilibrio ? "equilibrada" : "desbalanceada")
                        + ". División elegida: " + cantidadSi + " sí / "
                        + (cantidad - cantidadSi) + " no.");
                System.out.println("[" + nombre + "] Si responde SÍ, descarto " + (cantidad - cantidadSi)
                        + "; si responde NO, descarto " + cantidadSi + ".");
                System.out.println("[" + nombre + "] Pregunta: \"" + pregunta + "\"");
                boolean respuesta = arbitro.responder(pregunta);
                System.out.println("[" + nombre + "] Respuesta recibida: " + (respuesta ? "SÍ" : "NO"));
                int eliminados = tablero.descartarSegun(pregunta, respuesta);
                System.out.println("[" + nombre + "] Descartó " + eliminados
                        + " candidatos. Le quedan " + tablero.getCantidadViva() + " candidatos.");
                // Preguntar termina el turno, aunque haya quedado un solo candidato.
                return false;
            }
            System.out.println("[" + nombre + "] No hay preguntas útiles: debe arriesgar.");
        } else {
            System.out.println("[" + nombre + "] El sorteo indica arriesgar: no hago una pregunta este turno.");
        }

        List<Personaje> vivos = new ArrayList<>();
        for (Personaje personaje : mazo) {
            if (tablero.estaVivo(personaje.getId())) {
                vivos.add(personaje);
            }
        }
        System.out.println("[" + nombre + "] Elijo al azar entre mis " + vivos.size()
                + " candidatos vivos; cada uno tiene 1 posibilidad entre " + vivos.size() + ".");
        return arriesgar(nombre, tablero, vivos.get(random.nextInt(vivos.size())), arbitro);
    }

    private boolean arriesgar(String nombre, TableroCandidatos tablero,
            Personaje candidato, IArbitroTurno arbitro) {
        System.out.println("[" + nombre + "] Arriesga por: " + candidato.getNombre()
                + " (ID: " + candidato.getId() + ")");
        if (arbitro.comprobarIntento(candidato.getId())) {
            System.out.println("\n**************************************************");
            System.out.println("   ¡" + nombre.toUpperCase() + " HA ADIVINADO EL PERSONAJE!   ");
            System.out.println("   El personaje era: " + candidato.getNombre()
                    + " (ID: " + candidato.getId() + ")");
            System.out.println("**************************************************\n");
            return true;
        }
        tablero.descartar(candidato.getId());
        System.out.println("[" + nombre + "] Falló la adivinanza. Descartó a " + candidato.getNombre()
                + ". Le quedan " + tablero.getCantidadViva() + " candidatos.");
        return false;
    }
}
