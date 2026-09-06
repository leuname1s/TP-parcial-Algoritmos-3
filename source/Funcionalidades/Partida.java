package Funcionalidades;

import datos.MazoPersonajes;
import defaults.CatalogoPersonajes;

import datos.ModoJuego;
import datos.Personaje;
import datos.Pregunta;
import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import Interfaces.IPartida;
import Interfaces.ITableroCandidatos;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Partida implements IPartida {

    private final Scanner scanner;
    private final Random random;
    private MazoPersonajes mazo;
    private ModoJuego modoActual;

    private Personaje personajeJugador;
    private Personaje objetivoMaquina1;
    private Personaje objetivoMaquina2;

    private TableroCandidatos tableroJugador;
    private final EnumSet<Pregunta> preguntasJugador = EnumSet.noneOf(Pregunta.class);

    private IMaquina maquina1;
    private IMaquina maquina2;

    public Partida() {
        this(new Scanner(System.in));
    }

    public Partida(Scanner scanner) {
        this(scanner, new Random());
    }

    public Partida(Scanner scanner, Random random) {
        this.scanner = scanner;
        this.random = random;
    }

    @Override
    public void iniciar(ModoJuego modo) {
        this.modoActual = modo;
        this.personajeJugador = null;
        this.objetivoMaquina1 = null;
        this.objetivoMaquina2 = null;
        this.tableroJugador = null;
        this.maquina1 = null;
        this.maquina2 = null;
        this.preguntasJugador.clear();
        System.out.println("\n==========================================");
        System.out.println("       CONFIGURACIÓN INICIAL DE PARTIDA   ");
        System.out.println("==========================================");
        System.out.println("Modo seleccionado: " + modo.getDescripcion());

        // Keep the sampled deck throughout both phases.
        this.mazo = CatalogoPersonajes.crearMazo(random);

        if (modo == ModoJuego.JUGADOR_VS_MAQUINA_1) {
            configurarJugadorVsMaquina1();
        } else if (modo == ModoJuego.JUGADOR_VS_MAQUINA_2) {
            configurarJugadorVsMaquina2();
        } else if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            configurarModoMaquinaVsMaquina();
        }

        resumenPartidaInicializada(modo);
    }

    private void configurarJugadorVsMaquina1() {
        this.personajeJugador = seleccionarPersonajeJugador();
        this.tableroJugador = new TableroCandidatos(mazo);

        this.objetivoMaquina1 = sortearPersonaje(null);
        this.maquina1 = new Maquina1(mazo);
    }

    private void configurarJugadorVsMaquina2() {
        this.personajeJugador = seleccionarPersonajeJugador();
        this.tableroJugador = new TableroCandidatos(mazo);

        this.objetivoMaquina2 = sortearPersonaje(null);
        this.maquina2 = new Maquina2(mazo);
    }

    private void configurarModoMaquinaVsMaquina() {
        this.objetivoMaquina1 = sortearPersonaje(null);
        this.objetivoMaquina2 = sortearPersonaje(objetivoMaquina1);

        this.maquina1 = new Maquina1(mazo);
        this.maquina2 = new Maquina2(mazo);
    }

    private Personaje sortearPersonaje(Personaje excluido) {
        List<Personaje> disponibles = new ArrayList<>();
        for (Personaje p : mazo) {
            if (!p.equals(excluido)) {
                disponibles.add(p);
            }
        }
        return disponibles.get(random.nextInt(disponibles.size()));
    }

    private Personaje seleccionarPersonajeJugador() {
        System.out.println("\n¿Cómo deseas elegir a tu personaje secreto?");
        System.out.println("  1. Elegir manualmente del mazo de esta partida");
        System.out.println("  2. Asignación aleatoria (Omitir selección)");
        System.out.print("Ingrese una opción (1 o 2): ");

        while (true) {
            String opcion = scanner.nextLine().trim();
            if ("1".equals(opcion)) {
                return elegirManualmente();
            } else if ("2".equals(opcion)) {
                Personaje p = sortearPersonaje(null);
                System.out.println("\n[!] Se te ha asignado aleatoriamente el personaje: " + p.getNombre() + " (ID: " + p.getId() + ")");
                return p;
            } else {
                System.out.print("[!] Opción inválida. Ingrese 1 o 2: ");
            }
        }
    }

    private Personaje elegirManualmente() {
        System.out.println("\n--- PERSONAJES DISPONIBLES EN ESTA PARTIDA ---");
        for (Personaje p : mazo) {
            System.out.println(p);
        }
        System.out.println("------------------------------------------");
        System.out.print("Ingrese uno de los ID mostrados: ");

        while (true) {
            String entrada = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(entrada);
                Personaje seleccionado = mazo.buscarPorId(id);
                if (seleccionado != null) {
                    System.out.println("\n[✓] Has elegido a: " + seleccionado.getNombre());
                    return seleccionado;
                } else {
                    System.out.print("[!] Ese ID no pertenece a esta partida. Ingrese uno de los ID mostrados: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Entrada no válida. Ingrese uno de los ID mostrados: ");
            }
        }
    }

    private void resumenPartidaInicializada(ModoJuego modo) {
        System.out.println("\n------------------------------------------");
        System.out.println("   PARTIDA INICIALIZADA CORRECTAMENTE     ");
        System.out.println("------------------------------------------");
        if (personajeJugador != null) {
            System.out.println("Tu personaje secreto: " + personajeJugador.getNombre() + " (ID: " + personajeJugador.getId() + ")");
            System.out.println("Tablero del Jugador: " + tableroJugador.getCantidadViva() + " candidatos vivos.");
        }
        if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2 && objetivoMaquina1 != null) {
            System.out.println("Objetivo secreto de Máquina 1 (a adivinar): " + objetivoMaquina1.getNombre() + " (ID: " + objetivoMaquina1.getId() + ")");
        }
        if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2 && objetivoMaquina2 != null) {
            System.out.println("Objetivo secreto de Máquina 2 (a adivinar): " + objetivoMaquina2.getNombre() + " (ID: " + objetivoMaquina2.getId() + ")");
        }
        System.out.println("------------------------------------------\n");
    }

    @Override
    public void jugar() {
        if (modoActual == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            jugarMaquinaVsMaquina();
            return;
        }

        // FASE 1: Enfrentamiento inicial
        IMaquina maquinaEnemigaInicial = (modoActual == ModoJuego.JUGADOR_VS_MAQUINA_1) ? maquina1 : maquina2;
        Personaje objetivoEnemigoInicial = (modoActual == ModoJuego.JUGADOR_VS_MAQUINA_1) ? objetivoMaquina1 : objetivoMaquina2;

        boolean ganoJugadorFase1 = ejecutarBuclePartida(maquinaEnemigaInicial, objetivoEnemigoInicial);

        // Only a first-phase victory can unlock the optional challenge.
        if (ganoJugadorFase1) {
            boolean quiereContinuar = ofrecerDesafioSegundaMaquina(maquinaEnemigaInicial);
            if (quiereContinuar) {
                iniciarYJugarFase2(maquinaEnemigaInicial);
            }
        }
    }

    private boolean ofrecerDesafioSegundaMaquina(IMaquina maquinaDerrotada) {
        String nombreSiguiente = maquinaDerrotada.getNombre().equals("Máquina 1") ? "Máquina 2" : "Máquina 1";

        System.out.println("\n==========================================");
        System.out.println("   ¡VICTORIA CONTRA " + maquinaDerrotada.getNombre().toUpperCase() + "!   ");
        System.out.println("==========================================");
        int descartados = mazo.getCantidad() - maquinaDerrotada.getTablero().getCantidadViva();
        if (descartados >= 15) {
            System.out.println("La máquina descartó " + descartados
                    + " personajes. La segunda fase requiere menos de 15 descartes.");
            System.out.println("La partida finaliza con tu victoria en la primera fase.");
            return false;
        }
        System.out.println("¿Deseas continuar en esta misma partida desafiando a " + nombreSiguiente + "?");
        System.out.println("  1. Sí, pelear contra " + nombreSiguiente);
        System.out.println("  2. No, finalizar la partida");
        System.out.print("Ingrese una opción (1 o 2): ");

        while (true) {
            String opcion = scanner.nextLine().trim();
            if ("1".equals(opcion)) {
                return true;
            } else if ("2".equals(opcion)) {
                System.out.println("\nHas decidido finalizar la partida. ¡Gran trabajo!");
                return false;
            } else {
                System.out.print("[!] Opción inválida. Ingrese 1 o 2: ");
            }
        }
    }

    private void iniciarYJugarFase2(IMaquina maquinaPrimera) {
        TableroCandidatos tableroHeredado = (TableroCandidatos) maquinaPrimera.getTablero();
        IMaquina segundaMaquina;
        Personaje nuevoObjetivo;
        Personaje objetivoAnterior = maquinaPrimera == maquina1 ? objetivoMaquina1 : objetivoMaquina2;

        if (maquinaPrimera.getNombre().equals("Máquina 1")) {
            this.maquina2 = new Maquina2(mazo, tableroHeredado);
            this.objetivoMaquina2 = sortearPersonaje(objetivoAnterior);
            segundaMaquina = this.maquina2;
            nuevoObjetivo = this.objetivoMaquina2;
        } else {
            this.maquina1 = new Maquina1(mazo, tableroHeredado);
            this.objetivoMaquina1 = sortearPersonaje(objetivoAnterior);
            segundaMaquina = this.maquina1;
            nuevoObjetivo = this.objetivoMaquina1;
        }

        // The previous machine's secret cannot be selected again.
        this.tableroJugador = new TableroCandidatos(mazo);
        this.tableroJugador.descartar(objetivoAnterior.getId());
        this.preguntasJugador.clear();

        System.out.println("\n==========================================");
        System.out.println("       INICIANDO FASE 2 DE LA PARTIDA     ");
        System.out.println("==========================================");
        System.out.println("- Enfrentas a: " + segundaMaquina.getNombre());
        System.out.println("- Tu personaje secreto se mantiene: " + personajeJugador.getNombre() + " (ID: " + personajeJugador.getId() + ")");
        System.out.println("- Tu tablero de candidatos ha sido RESETEADO ("
                + tableroJugador.getCantidadViva() + " vivos).");
        System.out.println("- Se descartó el personaje anterior: " + objetivoAnterior.getNombre()
                + " (ID: " + objetivoAnterior.getId() + ").");
        System.out.println("- " + segundaMaquina.getNombre() + " HEREDÓ el tablero de " + maquinaPrimera.getNombre()
                + " (posee " + segundaMaquina.getTablero().getCantidadViva() + " candidatos vivos).");
        System.out.println("- " + segundaMaquina.getNombre() + " ha recibido un nuevo personaje objetivo secreto.");
        System.out.println("==========================================\n");

        ejecutarBuclePartida(segundaMaquina, nuevoObjetivo);
    }

    private boolean ejecutarBuclePartida(IMaquina maquinaEnemiga, Personaje objetivoEnemigo) {
        boolean finDeJuego = false;
        boolean ganoJugador = false;
        int ronda = 1;

        while (!finDeJuego) {
            System.out.println("\n==========================================");
            System.out.println("               RONDA " + ronda);
            System.out.println("==========================================");

            // Turno del jugador
            boolean victoriaJugador = turnoJugador(objetivoEnemigo);
            if (victoriaJugador) {
                ganoJugador = true;
                finDeJuego = true;
                break;
            }

            // Turno de la máquina enemiga
            boolean victoriaMaquina = ejecutarTurnoMaquina(maquinaEnemiga, personajeJugador);
            if (victoriaMaquina) {
                ganoJugador = false;
                finDeJuego = true;
                break;
            }

            ronda++;
        }

        return ganoJugador;
    }

    private boolean ejecutarTurnoMaquina(IMaquina maquina, final Personaje objetivoEnemigo) {
        return maquina.ejecutarTurno(new IArbitroTurno() {
            @Override
            public boolean responder(Pregunta pregunta) {
                return pregunta.cumple(objetivoEnemigo);
            }

            @Override
            public boolean comprobarIntento(int id) {
                return id == objetivoEnemigo.getId();
            }
        });
    }

    private boolean turnoJugador(Personaje objetivoEnemigo) {
        System.out.println("\n------------------------------------------");
        System.out.println("            TURNO DEL JUGADOR             ");
        System.out.println("------------------------------------------");
        System.out.println("Candidatos vivos restantes en tu tablero: " + tableroJugador.getCantidadViva());
        System.out.println("  1. Hacer una pregunta");
        System.out.println("  2. Arriesgar personaje por ID");
        System.out.println("  3. Ver candidatos vivos");
        System.out.print("Seleccione una opción: ");

        while (true) {
            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1":
                    if (hacerPreguntaJugador(objetivoEnemigo)) {
                        return false;
                    }
                    System.out.print("Seleccione 2 para arriesgar o 3 para ver candidatos: ");
                    break;
                case "2":
                    return arriesgarJugador(objetivoEnemigo);
                case "3":
                    mostrarCandidatosVivos();
                    System.out.println("\n¿Qué deseas hacer ahora?");
                    System.out.println("  1. Hacer una pregunta");
                    System.out.println("  2. Arriesgar personaje por ID");
                    System.out.println("  3. Ver candidatos vivos");
                    System.out.print("Seleccione una opción: ");
                    break;
                default:
                    System.out.print("[!] Opción inválida. Ingrese 1, 2 o 3: ");
                    break;
            }
        }
    }

    private boolean hacerPreguntaJugador(Personaje objetivoEnemigo) {
        if (preguntasJugador.size() == Pregunta.values().length) {
            System.out.println("[!] Ya realizaste todas las preguntas en esta fase. No se consume el turno.");
            return false;
        }
        System.out.println("\n--- ELIGE UNA PREGUNTA ---");
        Pregunta[] preguntas = Pregunta.values();
        for (int i = 0; i < preguntas.length; i++) {
            System.out.printf("  %d. %s%s%n", i + 1, preguntas[i],
                    preguntasJugador.contains(preguntas[i]) ? " [ya realizada]" : "");
        }
        System.out.print("Ingrese el número de la pregunta (1 a " + preguntas.length + "): ");

        int indexPregunta = -1;
        while (indexPregunta == -1) {
            String entrada = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(entrada);
                if (num >= 1 && num <= preguntas.length) {
                    if (preguntasJugador.contains(preguntas[num - 1])) {
                        System.out.print("[!] Ya realizaste esa pregunta. Elegí otra; no se consume el turno: ");
                    } else {
                        indexPregunta = num - 1;
                    }
                } else {
                    System.out.print("[!] Número fuera de rango. Ingrese un número entre 1 y " + preguntas.length + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Entrada no válida. Ingrese un número entre 1 y " + preguntas.length + ": ");
            }
        }

        Pregunta q = preguntas[indexPregunta];
        boolean respuesta = q.cumple(objetivoEnemigo);
        preguntasJugador.add(q);
        System.out.println("\n -> Pregunta realizada: \"" + q + "\"");
        System.out.println(" -> Respuesta de la máquina: " + (respuesta ? "¡SÍ!" : "NO"));

        int descartados = tableroJugador.descartarSegun(q, respuesta);
        System.out.println(" -> Se descartaron " + descartados + " candidatos de tu tablero.");
        System.out.println(" -> Candidatos vivos restantes: " + tableroJugador.getCantidadViva());
        return true;
    }

    private boolean arriesgarJugador(Personaje objetivoEnemigo) {
        System.out.print("\nIngrese el ID de un candidato vivo de esta partida: ");
        int idArriesgado = -1;
        while (idArriesgado == -1) {
            String entrada = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(entrada);
                if (mazo.buscarPorId(id) == null) {
                    System.out.print("[!] Ese ID no pertenece a esta partida. Ingrese otro ID: ");
                } else if (!tableroJugador.estaVivo(id)) {
                    System.out.print("[!] Ese personaje ya fue descartado. Ingrese un candidato vivo: ");
                } else {
                    idArriesgado = id;
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Entrada no válida. Ingrese el ID de un candidato vivo: ");
            }
        }

        Personaje pArriesgado = mazo.buscarPorId(idArriesgado);
        if (idArriesgado == objetivoEnemigo.getId()) {
            System.out.println("\n**************************************************");
            System.out.println("   ¡CORRECTO! ¡HAS ADIVINADO EL PERSONAJE!        ");
            System.out.println("   El personaje secreto era: " + pArriesgado.getNombre() + " (ID: " + pArriesgado.getId() + ")");
            System.out.println("**************************************************\n");
            return true;
        } else {
            System.out.println("\n[X] INCORRECTO. " + pArriesgado.getNombre() + " NO es el personaje secreto.");
            if (tableroJugador.estaVivo(idArriesgado)) {
                tableroJugador.descartar(idArriesgado);
                System.out.println("Se descartó a " + pArriesgado.getNombre() + " de tu tablero. Quedan " + tableroJugador.getCantidadViva() + " candidatos.");
            }
            return false;
        }
    }

    private void mostrarCandidatosVivos() {
        System.out.println("\n--- CANDIDATOS VIVOS EN TU TABLERO (" + tableroJugador.getCantidadViva() + ") ---");
        for (Personaje p : mazo) {
            if (tableroJugador.estaVivo(p.getId())) {
                System.out.println(p);
            }
        }
        System.out.println("--------------------------------------------------");
    }

    private void jugarMaquinaVsMaquina() {
        System.out.println("\n--- INICIANDO MODO MÁQUINA 1 VS MÁQUINA 2 ---");
        boolean finDeJuego = false;
        int ronda = 1;

        while (!finDeJuego) {
            System.out.println("\n==========================================");
            System.out.println("               RONDA " + ronda);
            System.out.println("==========================================");

            // Turno de Máquina 1 buscando a objetivoMaquina2
            finDeJuego = ejecutarTurnoMaquina(maquina1, objetivoMaquina2);
            if (finDeJuego) {
                break;
            }

            // Turno de Máquina 2 buscando a objetivoMaquina1
            finDeJuego = ejecutarTurnoMaquina(maquina2, objetivoMaquina1);
            if (finDeJuego) {
                break;
            }

            ronda++;
        }
    }

    @Override
    public Personaje getPersonajeJugador() {
        return personajeJugador;
    }

    @Override
    public Personaje getObjetivoMaquina1() {
        return objetivoMaquina1;
    }

    @Override
    public Personaje getObjetivoMaquina2() {
        return objetivoMaquina2;
    }

    @Override
    public ITableroCandidatos getTableroJugador() {
        return tableroJugador;
    }

    @Override
    public ITableroCandidatos getTableroMaquina1() {
        return (maquina1 != null) ? maquina1.getTablero() : null;
    }

    @Override
    public ITableroCandidatos getTableroMaquina2() {
        return (maquina2 != null) ? maquina2.getTablero() : null;
    }
}
