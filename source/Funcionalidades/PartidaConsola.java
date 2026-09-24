package Funcionalidades;

import Interfaces.IPartida;
import datos.DiagnosticoTurno;
import datos.DiagnosticoTurno.Comparacion;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoTurno;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

/** Adaptador de consola: interpreta entradas y presenta los datos que devuelve el motor. */
public final class PartidaConsola {
    private final IPartida partida;
    private final Scanner scanner;
    private final PrintStream salida;

    public PartidaConsola(Scanner scanner) { this(new Partida(), scanner, System.out); }

    public PartidaConsola(IPartida partida, Scanner scanner, PrintStream salida) {
        this.partida = Objects.requireNonNull(partida);
        this.scanner = Objects.requireNonNull(scanner);
        this.salida = Objects.requireNonNull(salida);
    }

    public void jugar(ModoJuego modo) {
        partida.iniciar(modo);
        salida.println("\n==========================================");
        salida.println("       CONFIGURACIÓN INICIAL DE PARTIDA");
        salida.println("==========================================");
        salida.println("Modo seleccionado: " + modo.getDescripcion());
        if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            for (Participante maquina : new Participante[] {Participante.MAQUINA_1, Participante.MAQUINA_2}) {
                salida.println("Personaje secreto de " + maquina.getNombre() + ": "
                        + partida.getSecretoEspectador(maquina));
            }
        }
        continuar();
    }

    /** Permite retomar el estado actual; un fin de entrada deja la partida incompleta. */
    public void continuar() {
        if (partida.getEstado() == EstadoPartida.SIN_INICIAR) {
            throw new IllegalStateException("Primero debe iniciar la partida");
        }
        while (partida.getEstado() != EstadoPartida.FINALIZADA) {
            switch (partida.getEstado()) {
                case SELECCION_PERSONAJE:
                    if (!seleccionarPersonaje()) { return; }
                    salida.println("Tu personaje secreto: " + partida.getPersonajeJugador());
                    break;
                case DECISION_SEGUNDA_FASE:
                    salida.println("¿Deseas continuar en esta misma partida desafiando a la segunda máquina?");
                    Integer decision = leerOpcion("1. Sí / 2. No: ", 1, 2);
                    if (decision == null) { return; }
                    partida.decidirSegundaFase(decision == 1);
                    if (decision == 1) {
                        salida.println("\nINICIANDO FASE 2 DE LA PARTIDA");
                        salida.println("Enfrentas a: " + partida.getRivalActual().getNombre());
                        salida.println("Tu personaje secreto se mantiene: " + partida.getPersonajeJugador());
                        salida.println("Tu tablero comienza con "
                                + partida.getCandidatos(Participante.JUGADOR).size() + " candidatos.");
                        salida.println("La nueva máquina heredó "
                                + partida.getCandidatos(partida.getRivalActual()).size() + " candidatos.");
                    }
                    break;
                case EN_CURSO:
                    salida.println("\nFASE " + partida.getFase() + " - RONDA " + partida.getRonda());
                    if (partida.getTurno() == Participante.JUGADOR) {
                        if (!turnoJugador()) { return; }
                    } else {
                        mostrarTurno(partida.ejecutarTurnoMaquina());
                    }
                    break;
                default:
                    throw new IllegalStateException("Estado de partida no atendido");
            }
        }
        mostrarResultado();
    }

    private boolean seleccionarPersonaje() {
        Integer opcion = leerOpcion("Personaje secreto: 1. Elegir por ID / 2. Aleatorio: ", 1, 2);
        if (opcion == null) { return false; }
        if (opcion == 2) {
            partida.seleccionarPersonajeAleatorio();
            return true;
        }
        for (Personaje personaje : partida.getPersonajes()) { salida.println(personaje); }
        while (true) {
            Integer id = leerEntero("Ingrese uno de los ID mostrados: ");
            if (id == null) { return false; }
            try {
                partida.seleccionarPersonaje(id);
                return true;
            } catch (IllegalArgumentException ex) {
                salida.println("[!] " + ex.getMessage());
            }
        }
    }

    private boolean turnoJugador() {
        salida.println("TURNO DEL JUGADOR - Candidatos vivos: "
                + partida.getCandidatos(Participante.JUGADOR).size());
        Integer opcion = leerOpcion("1. Preguntar / 2. Arriesgar por ID / 3. Ver candidatos: ", 1, 3);
        if (opcion == null) { return false; }
        if (opcion == 3) {
            for (Personaje personaje : partida.getCandidatos(Participante.JUGADOR)) {
                salida.println(personaje);
            }
            return true;
        }
        if (opcion == 2) {
            while (true) {
                Integer id = leerEntero("Ingrese el ID de un candidato vivo: ");
                if (id == null) { return false; }
                try {
                    mostrarTurno(partida.arriesgar(id));
                    return true;
                } catch (IllegalArgumentException ex) {
                    salida.println("[!] " + ex.getMessage() + ". No se consume el turno.");
                }
            }
        }
        Pregunta[] preguntas = Pregunta.values();
        if (partida.getPreguntasRealizadas().size() == preguntas.length) {
            salida.println("[!] Ya realizaste todas las preguntas en esta fase. No se consume el turno.");
            return true;
        }
        for (int i = 0; i < preguntas.length; i++) {
            salida.println((i + 1) + ". " + preguntas[i]
                    + (partida.getPreguntasRealizadas().contains(preguntas[i]) ? " [ya realizada]" : ""));
        }
        while (true) {
            Integer numero = leerOpcion("Ingrese el número de la pregunta: ", 1, preguntas.length);
            if (numero == null) { return false; }
            try {
                mostrarTurno(partida.preguntar(preguntas[numero - 1]));
                return true;
            } catch (IllegalArgumentException ex) {
                salida.println("[!] " + ex.getMessage());
            }
        }
    }

    private Integer leerOpcion(String mensaje, int minimo, int maximo) {
        while (true) {
            Integer valor = leerEntero(mensaje);
            if (valor == null || (valor >= minimo && valor <= maximo)) { return valor; }
            salida.println("[!] Opción fuera de rango. Ingrese un número entre " + minimo + " y " + maximo);
        }
    }

    private Integer leerEntero(String mensaje) {
        while (true) {
            salida.print(mensaje);
            if (!scanner.hasNextLine()) { return null; }
            try {
                return Integer.valueOf(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                salida.println("[!] Entrada no válida. Ingrese un número.");
            }
        }
    }

    public void mostrarTurno(ResultadoTurno accion) {
        String prefijo = "[" + accion.getParticipante() + "] ";
        salida.println("\n--- TURNO DE " + accion.getParticipante().toUpperCase() + " ---");
        if (accion.getDiagnostico() != null) { mostrarDiagnostico(accion, prefijo); }
        if (accion.getPregunta() != null) {
            salida.println(prefijo + "Pregunta realizada: " + accion.getPregunta());
            salida.println(prefijo + "Respuesta recibida: " + (accion.getRespuesta() ? "SÍ" : "NO"));
        } else {
            salida.println(prefijo + "Arriesga por: " + accion.getIntento());
            salida.println(accion.isAcierto()
                    ? prefijo + "¡HA ADIVINADO EL PERSONAJE!"
                    : prefijo + "INCORRECTO. Se descarta el personaje intentado.");
        }
        salida.println(prefijo + "Descartó " + accion.getDescartados() + " candidatos. Le quedan "
                + accion.getCandidatosRestantes() + " candidatos.");
    }

    private void mostrarDiagnostico(ResultadoTurno accion, String prefijo) {
        DiagnosticoTurno diagnostico = accion.getDiagnostico();
        salida.println(prefijo + "Candidatos: " + diagnostico.getCandidatosAntes()
                + "; descartados: " + diagnostico.getDescartadosAntes()
                + "; probabilidad de arriesgar: " + diagnostico.getPorcentajeRiesgo() + "%.");
        salida.println(prefijo + "Cálculo: 4% + " + diagnostico.getIncremento()
                + " puntos por descarte (incluidos los heredados), con un máximo de 100%.");
        if (diagnostico.getSorteo() != null) {
            salida.println(prefijo + "Sorteo de riesgo: " + diagnostico.getSorteo() * 100.0
                    + "; arriesga si es menor que " + diagnostico.getPorcentajeRiesgo() + ".");
        }
        switch (diagnostico.getMotivo()) {
            case CANDIDATO_UNICO:
                salida.println(prefijo + "Queda un único candidato: intento obligatorio.");
                break;
            case SORTEO_RIESGO:
                salida.println(prefijo + "El sorteo indica arriesgar: no hago una pregunta este turno.");
                break;
            case PREGUNTA:
            case SIN_PREGUNTAS:
                salida.println(prefijo + "El sorteo no indica arriesgar: busco una pregunta.");
                salida.println(prefijo + "Comparo todas las preguntas. Busco la "
                        + (diagnostico.isBuscaEquilibrio() ? "menor" : "mayor") + " diferencia entre sí y no.");
                for (Comparacion comparacion : diagnostico.getComparaciones()) {
                    salida.println(prefijo + comparacion.getPregunta() + " -> sí: "
                            + comparacion.getCantidadSi() + ", no: " + comparacion.getCantidadNo()
                            + (comparacion.esConstante() ? ". La ignoro: todos responderían lo mismo."
                            : ", diferencia: " + comparacion.getDiferencia() + "."));
                }
                if (diagnostico.getMotivo() == DiagnosticoTurno.Motivo.SIN_PREGUNTAS) {
                    salida.println(prefijo + "No hay preguntas útiles: debe arriesgar.");
                } else {
                    salida.println(prefijo + "Mejores preguntas: " + diagnostico.getMejoresPreguntas());
                    salida.println(prefijo + (diagnostico.getMejoresPreguntas().size() > 1
                            ? "Desempato al azar: " : "Única mejor pregunta: ") + accion.getPregunta());
                    for (Comparacion comparacion : diagnostico.getComparaciones()) {
                        if (comparacion.getPregunta() == accion.getPregunta()) {
                            salida.println(prefijo + "Mejor diferencia: " + comparacion.getDiferencia()
                                    + ". Si responde SÍ, descarto " + comparacion.getCantidadNo()
                                    + "; si responde NO, descarto " + comparacion.getCantidadSi() + ".");
                        }
                    }
                }
                break;
        }
        if (accion.getIntento() != null && diagnostico.getCandidatosAntes() > 1) {
            salida.println(prefijo + "Elijo al azar entre los candidatos vivos; cada uno tiene 1 posibilidad entre "
                    + diagnostico.getCandidatosAntes() + ".");
        }
    }

    private void mostrarResultado() {
        switch (partida.getResultado().getDesenlace()) {
            case VICTORIA:
                salida.println("¡Victoria en la primera fase!");
                break;
            case VICTORIA_VERDADERA: salida.println("¡Victoria verdadera: ganaste ambas fases!"); break;
            case DERROTA: salida.println("La partida termina con derrota."); break;
            case GANA_MAQUINA_1: salida.println("Ganó Máquina 1."); break;
            case GANA_MAQUINA_2: salida.println("Ganó Máquina 2."); break;
        }
    }
}
