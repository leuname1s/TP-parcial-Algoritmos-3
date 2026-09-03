package funcionalidades;

import datos.ModoJuego;
import datos.Personaje;
import interfaces.IPartida;
import interfaces.ITableroCandidatos;

import java.util.Random;
import java.util.Scanner;

public class Partida implements IPartida {

    private final Scanner scanner;
    private final Random random;
    private MazoPersonajes mazo;

    private Personaje personajeJugador;
    private Personaje objetivoMaquina1;
    private Personaje objetivoMaquina2;

    private TableroCandidatos tableroJugador;
    private TableroCandidatos tableroMaquina1;
    private TableroCandidatos tableroMaquina2;

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
        System.out.println("\n==========================================");
        System.out.println("       CONFIGURACIÓN INICIAL DE PARTIDA   ");
        System.out.println("==========================================");
        System.out.println("Modo seleccionado: " + modo.getDescripcion());

        // Cargar mazo completo
        this.mazo = CatalogoPersonajes.crearMazo();

        if (modo == ModoJuego.JUGADOR_VS_MAQUINA_1 || modo == ModoJuego.JUGADOR_VS_MAQUINA_2) {
            configurarModoJugador(modo);
        } else if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
            configurarModoMaquinaVsMaquina();
        }

        resumenPartidaInicializada(modo);
    }

    private void configurarModoJugador(ModoJuego modo) {
        // 1. Selección de personaje del jugador
        this.personajeJugador = seleccionarPersonajeJugador();
        this.tableroJugador = new TableroCandidatos(mazo);

        // 2. Asignación aleatoria del objetivo secreto de la máquina
        int idObjetivoMaq = random.nextInt(MazoPersonajes.TOTAL) + 1;
        this.objetivoMaquina1 = mazo.buscarPorId(idObjetivoMaq);
        this.tableroMaquina1 = new TableroCandidatos(mazo);
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

    private void configurarModoMaquinaVsMaquina() {
        // Asignación de objetivos secretos aleatorios para ambas máquinas
        int id1 = random.nextInt(MazoPersonajes.TOTAL) + 1;
        int id2 = random.nextInt(MazoPersonajes.TOTAL) + 1;

        this.objetivoMaquina1 = mazo.buscarPorId(id1);
        this.objetivoMaquina2 = mazo.buscarPorId(id2);

        this.tableroMaquina1 = new TableroCandidatos(mazo);
        this.tableroMaquina2 = new TableroCandidatos(mazo);
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
            System.out.println("Tablero de Máquina 1: " + tableroMaquina1.getCantidadViva() + " candidatos vivos.");
        }
        if (objetivoMaquina2 != null) {
            System.out.println("Objetivo secreto de Máquina 2 (a adivinar): " + objetivoMaquina2.getNombre() + " (ID: " + objetivoMaquina2.getId() + ")");
            System.out.println("Tablero de Máquina 2: " + tableroMaquina2.getCantidadViva() + " candidatos vivos.");
        }
        System.out.println("------------------------------------------\n");
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
        return tableroMaquina1;
    }

    @Override
    public ITableroCandidatos getTableroMaquina2() {
        return tableroMaquina2;
    }
}
