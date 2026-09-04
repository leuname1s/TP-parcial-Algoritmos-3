package funcionalidades;

import datos.ModoJuego;
import interfaces.IMenu;
import interfaces.IPartida;

import java.util.Scanner;

public class MenuConsola implements IMenu {

    private final Scanner scanner;

    public MenuConsola() {
        this.scanner = new Scanner(System.in);
    }

    public MenuConsola(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void mostrar() {
        System.out.println("==========================================");
        System.out.println("          JUEGO: ADIVINANZAS              ");
        System.out.println("==========================================");
        System.out.println("Seleccione el modo de juego:");
        System.out.println("  1. Jugador vs Máquina 1");
        System.out.println("  2. Jugador vs Máquina 2");
        System.out.println("  3. Máquina 1 vs Máquina 2");
        System.out.println("  0. Salir");
        System.out.println("==========================================");
        System.out.print("Ingrese una opción: ");
    }

    @Override
    public ModoJuego seleccionarModo() {
        while (true) {
            mostrar();
            String entrada = scanner.nextLine().trim();
            try {
                int opcion = Integer.parseInt(entrada);
                ModoJuego modo = ModoJuego.obtenerPorOpcion(opcion);
                if (modo != null) {
                    return modo;
                } else {
                    System.out.println("\n[!] Opción inválida. Por favor, ingrese un número válido.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n[!] Entrada no válida. Debe ingresar un número.\n");
            }
        }
    }

    @Override
    public void ejecutarMenu() {
        ModoJuego modoSeleccionado;
        do {
            modoSeleccionado = seleccionarModo();
            switch (modoSeleccionado) {
                case JUGADOR_VS_MAQUINA_1:
                case JUGADOR_VS_MAQUINA_2:
                case MAQUINA_1_VS_MAQUINA_2:
                    iniciarModo(modoSeleccionado);
                    break;
                case SALIR:
                    System.out.println("\nGracias por jugar. ¡Hasta luego!");
                    break;
            }
        } while (modoSeleccionado != ModoJuego.SALIR);
    }

    private void iniciarModo(ModoJuego modo) {
        IPartida partida = new Partida(scanner);
        partida.iniciar(modo);
        partida.jugar();
    }
}
