package main;

import Funcionalidades.MenuConsola;
import Funcionalidades.Partida;
import Funcionalidades.ServicioEstadisticas;
import Controladores.ControladorJuego;
import GUI.VentanaPrincipal;
import datos.RepositorioEstadisticasArchivo;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        if (args.length == 1 && "--consola".equals(args[0])) {
            new MenuConsola().ejecutarMenu();
            return;
        }
        if (args.length != 0) {
            System.err.println("Uso: java -cp build main.Main [--consola]");
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("No hay un entorno gráfico disponible. Usá --consola.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ControladorJuego controlador = new ControladorJuego(new Partida(),
                    new ServicioEstadisticas(new RepositorioEstadisticasArchivo()), ventana);
            ventana.conectar(controlador);
            controlador.abrir();
            ventana.setVisible(true);
        });
    }
}
