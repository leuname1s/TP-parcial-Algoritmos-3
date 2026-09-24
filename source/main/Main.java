package main;

import Funcionalidades.MenuConsola;
import Funcionalidades.Partida;
import Funcionalidades.ServicioEstadisticas;
import Controladores.ControladorJuego;
import GUI.VentanaPrincipal;
import datos.RepositorioEstadisticasArchivo;
import datos.UsuarioArchivo;
import java.io.IOException;
import java.nio.file.Path;
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
            UsuarioArchivo archivoUsuario = new UsuarioArchivo(Path.of("data", "usuario.txt"));
            String ultimoUsuario = "";
            boolean usuarioLeido = false;
            String errorLectura = null;
            try {
                ultimoUsuario = archivoUsuario.cargar();
                usuarioLeido = true;
            } catch (IOException ex) {
                errorLectura = "No se pudo recuperar el último usuario: " + ex.getMessage();
            }
            // Si la lectura falla, conservar el archivo anterior para evitar perder el nombre.
            final boolean permitirGuardado = usuarioLeido;
            VentanaPrincipal ventana = new VentanaPrincipal(nombre -> {
                if (!permitirGuardado) { return; }
                try {
                    archivoUsuario.guardar(nombre);
                } catch (IOException ex) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "No se pudo guardar el último usuario: " + ex.getMessage(),
                            "Usuario", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            });
            ControladorJuego controlador = new ControladorJuego(new Partida(),
                    new ServicioEstadisticas(new RepositorioEstadisticasArchivo()), ventana);
            ventana.conectar(controlador);
            controlador.consultarEstadisticas(ultimoUsuario);
            ventana.setVisible(true);
            if (errorLectura != null) { ventana.mostrarError(errorLectura); }
        });
    }
}
