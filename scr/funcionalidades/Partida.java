package funcionalidades;

import datos.ModoJuego;
import datos.Personaje;
import datos.Pregunta;
import interfaces.IMaquina;
import interfaces.IPartida;
import interfaces.ITableroCandidatos;

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

    private IMaquina maquina1;
    private IMaquina maquina2;

    public Partida() {
        this.scanner = new Scanner(System.in);
        this.random = new Random();
    }

    public Partida(Scanner scanner) {
        this.scanner = scanner;
        this.random = new Random();
    }

    @Override
    public void iniciar(ModoJuego modo) {
        this.modoActual = modo;
        System.out.println("\n==========================================");
        System.out.println("       CONFIGURACIÓN INICIAL DE PARTIDA   ");
        System.out.println("==========================================");
        System.out.println("Modo seleccionado: " + modo.getDescripcion());

        // Cargar mazo completo
        this.mazo = CatalogoPersonajes.crearMazo();

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

        int idObjetivoMaq = random.nextInt(MazoPersonajes.TOTAL) + 1;
        this.objetivoMaquina1 = mazo.buscarPorId(idObjetivoMaq);
        this.maquina1 = new Maquina1(mazo);
    }

    private void configurarJugadorVsMaquina2() {
        this.personajeJugador = seleccionarPersonajeJugador();
        this.tableroJugador = new TableroCandidatos(mazo);

        int idObjetivoMaq = random.nextInt(MazoPersonajes.TOTAL) + 1;
        this.objetivoMaquina2 = mazo.buscarPorId(idObjetivoMaq);
        this.maquina2 = new Maquina2(mazo);
    }

    private void configurarModoMaquinaVsMaquina() {
        int id1 = random.nextInt(MazoPersonajes.TOTAL) + 1;
        int id2 = random.nextInt(MazoPersonajes.TOTAL) + 1;

        this.objetivoMaquina1 = mazo.buscarPorId(id1);
        this.objetivoMaquina2 = mazo.buscarPorId(id2);

        this.maquina1 = new Maquina1(mazo);
        this.maquina2 = new Maquina2(mazo);
    }

    private Personaje seleccionarPersonajeJugador() {
        System.out.println("\n¿Cómo deseas elegir a tu personaje secreto?");
        System.out.println("  1. Elegir manualmente del catálogo");
        System.out.println("  2. Asignación aleatoria (Omitir selección)");
        System.out.print("Ingrese una opción (1 o 2): ");

        while (true) {
            String opcion = scanner.nextLine().trim();
            if ("1".equals(opcion)) {
                return elegirManualmente();
            } else if ("2".equals(opcion)) {
                int idAleatorio = random.nextInt(MazoPersonajes.TOTAL) + 1;
                Personaje p = mazo.buscarPorId(idAleatorio);
                System.out.println("\n[!] Se te ha asignado aleatoriamente el personaje: " + p.getNombre() + " (ID: " + p.getId() + ")");
                return p;
            } else {
                System.out.print("[!] Opción inválida. Ingrese 1 o 2: ");
            }
        }
    }

    private Personaje elegirManualmente() {
        System.out.println("\n--- CATÁLOGO DE PERSONAJES DISPONIBLES ---");
        for (Personaje p : mazo) {
            System.out.println(p);
        }
        System.out.println("------------------------------------------");
        System.out.print("Ingrese el ID del personaje que desea elegir (1 a " + MazoPersonajes.TOTAL + "): ");

        while (true) {
            String entrada = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(entrada);
                Personaje seleccionado = mazo.buscarPorId(id);
                if (seleccionado != null) {
                    System.out.println("\n[✓] Has elegido a: " + seleccionado.getNombre());
                    return seleccionado;
                } else {
                    System.out.print("[!] ID fuera de rango. Ingrese un ID entre 1 y " + MazoPersonajes.TOTAL + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Entrada no válida. Debe ingresar un número entre 1 y " + MazoPersonajes.TOTAL + ": ");
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
        if (objetivoMaquina1 != null) {
            System.out.println("Objetivo secreto de Máquina 1 (a adivinar): " + objetivoMaquina1.getNombre() + " (ID: " + objetivoMaquina1.getId() + ")");
        }
        if (objetivoMaquina2 != null) {
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

        boolean finDeJuego = false;
        int ronda = 1;
        Personaje objetivoEnemigo = (modoActual == ModoJuego.JUGADOR_VS_MAQUINA_1) ? objetivoMaquina1 : objetivoMaquina2;
        IMaquina maquinaEnemiga = (modoActual == ModoJuego.JUGADOR_VS_MAQUINA_1) ? maquina1 : maquina2;

        while (!finDeJuego) {
            System.out.println("\n==========================================");
            System.out.println("               RONDA " + ronda);
            System.out.println("==========================================");

            // Turno del jugador
            finDeJuego = turnoJugador(objetivoEnemigo);
            if (finDeJuego) {
                break;
            }

            // Turno de la máquina correspondiente
            finDeJuego = maquinaEnemiga.ejecutarTurno(personajeJugador);
            if (finDeJuego) {
                break;
            }

            ronda++;
        }
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
                    hacerPreguntaJugador(objetivoEnemigo);
                    return false;
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

    private void hacerPreguntaJugador(Personaje objetivoEnemigo) {
        System.out.println("\n--- ELIGE UNA PREGUNTA ---");
        Pregunta[] preguntas = Pregunta.values();
        for (int i = 0; i < preguntas.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, preguntas[i]);
        }
        System.out.print("Ingrese el número de la pregunta (1 a " + preguntas.length + "): ");

        int indexPregunta = -1;
        while (indexPregunta == -1) {
            String entrada = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(entrada);
                if (num >= 1 && num <= preguntas.length) {
                    indexPregunta = num - 1;
                } else {
                    System.out.print("[!] Número fuera de rango. Ingrese un número entre 1 y " + preguntas.length + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Entrada no válida. Ingrese un número entre 1 y " + preguntas.length + ": ");
            }
        }

        Pregunta q = preguntas[indexPregunta];
        boolean respuesta = q.cumple(objetivoEnemigo);
        System.out.println("\n -> Pregunta realizada: \"" + q + "\"");
        System.out.println(" -> Respuesta de la máquina: " + (respuesta ? "¡SÍ!" : "NO"));

        int descartados = tableroJugador.descartarSegun(q, respuesta);
        System.out.println(" -> Se descartaron " + descartados + " candidatos de tu tablero.");
        System.out.println(" -> Candidatos vivos restantes: " + tableroJugador.getCantidadViva());
    }

    private boolean arriesgarJugador(Personaje objetivoEnemigo) {
        System.out.print("\nIngrese el ID del personaje por el cual desea arriesgar (1 a " + MazoPersonajes.TOTAL + "): ");
        int idArriesgado = -1;
        while (idArriesgado == -1) {
            String entrada = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(entrada);
                if (id >= 1 && id <= MazoPersonajes.TOTAL) {
                    idArriesgado = id;
                } else {
                    System.out.print("[!] ID fuera de rango. Ingrese un número entre 1 y " + MazoPersonajes.TOTAL + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Entrada no válida. Ingrese un número entre 1 y " + MazoPersonajes.TOTAL + ": ");
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
            finDeJuego = maquina1.ejecutarTurno(objetivoMaquina2);
            if (finDeJuego) {
                break;
            }

            // Turno de Máquina 2 buscando a objetivoMaquina1
            finDeJuego = maquina2.ejecutarTurno(objetivoMaquina1);
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
