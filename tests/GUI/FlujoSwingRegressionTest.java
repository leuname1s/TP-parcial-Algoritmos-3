package GUI;

import Controladores.ControladorJuego;
import Funcionalidades.Partida;
import Funcionalidades.ServicioEstadisticas;
import Interfaces.IRepositorioEstadisticas;
import datos.EstadoPartida;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import soporte.PartidaPrueba;
import static GUI.InterfazSwingRegressionTest.componentes;
import static GUI.InterfazSwingRegressionTest.distribuir;
import static soporte.PartidaPrueba.comprobar;

/** Flujos con la ventana real, sin mostrarla ni usar el archivo de estadísticas del usuario. */
public final class FlujoSwingRegressionTest {
    private FlujoSwingRegressionTest() { }

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("SKIP: la prueba de ventana requiere un entorno gráfico");
            return;
        }
        Path capturas = args.length == 0 ? null : Path.of(args[0]);
        if (capturas != null) Files.createDirectories(capturas);
        SwingUtilities.invokeAndWait(() -> {
            probarHumano(ModoJuego.JUGADOR_VS_MAQUINA_1, capturas);
            probarHumano(ModoJuego.JUGADOR_VS_MAQUINA_2, null);
            probarEspectador(capturas);
        });
        System.out.println("PASS: ventana Swing, ambos rivales, segunda fase, resultado y controles de espectador");
    }

    private static void probarHumano(ModoJuego modo, Path capturas) {
        Escenario caso = new Escenario();
        PartidaPrueba referencia = new PartidaPrueba(modo, true);
        try {
            caso.controlador.abrir();
            caso.pintar(capturas, "01-menu");
            componentes(caso.ventana, JTextField.class).get(0).setText("Ana");
            controlVisible(caso.ventana, modo == ModoJuego.JUGADOR_VS_MAQUINA_1
                    ? "Jugar contra Máquina 1" : "Jugar contra Máquina 2").doClick(0);
            caso.pintar(capturas, "02-seleccion");
            tarjetaVisible(caso.ventana, referencia.humano.getId()).doClick(0);
            // Elegir la carta solo prepara el secreto; el botón confirma la selección.
            comprobar(caso.partida.getEstado() == EstadoPartida.SELECCION_PERSONAJE,
                    "Un clic en una carta no debe comenzar la partida");
            controlVisible(caso.ventana, "Elegir personaje").doClick(0);
            caso.pintar(null, "inicio");
            Set<Integer> iniciales = verificarCandidatosMiniatura(caso);
            comprobar(iniciales.size() == 23, "La miniatura inicial debe mostrar los 23 candidatos de la máquina");
            controlVisible(caso.ventana, "Lentes").doClick(0);
            controlVisible(caso.ventana, "Usa lentes").doClick(0);
            controlVisible(caso.ventana, "Preguntar").doClick(0);
            comprobar(caso.partida.getTurno() != Participante.JUGADOR, "Preguntar debe pasar el turno");
            comprobar(!controlVisible(caso.ventana, "Preguntar").isEnabled(),
                    "La ventana debe bloquear preguntas durante el turno automático");
            comprobar(verificarCandidatosMiniatura(caso).equals(iniciales),
                    "La pregunta humana no debe descartar candidatos en la miniatura de la máquina");
            caso.reloj.avanzar();
            comprobar(caso.partida.getTurno() == Participante.JUGADOR, "Debe regresar el turno humano");
            Set<Integer> heredados = verificarCandidatosMiniatura(caso);
            comprobar(heredados.size() < iniciales.size(),
                    "El turno de la máquina debe actualizar sus descartes en la miniatura");
            caso.pintar(capturas, "03-partida-humana");
            verificarDistribucionLateral(caso.ventana);
            caso.pintar(capturas, "08-ventana-minima", 962, 622);
            verificarDistribucionLateral(caso.ventana);
            tarjetaVisible(caso.ventana, referencia.primerSecreto.getId()).doClick(0);
            controlVisible(caso.ventana, "Arriesgar personaje").doClick(0);
            comprobar(caso.partida.getEstado() == EstadoPartida.DECISION_SEGUNDA_FASE,
                    "El intento correcto debe presentar la segunda fase sin otro diálogo");
            caso.pintar(capturas, "04-segunda-fase");
            String secretoEsperado = referencia.primerSecreto.getNombre();
            if (modo == ModoJuego.JUGADOR_VS_MAQUINA_1) {
                caso.repositorio.fallarGuardado = true;
                controlVisible(caso.ventana, "Conservar victoria").doClick(0);
                caso.pintar(capturas, "09-error-guardado");
                comprobar(controlVisible(caso.ventana, "Reintentar guardado").isEnabled(),
                        "Un error debe permitir reintentar el guardado desde la ventana");
                controlVisible(caso.ventana, "Reintentar guardado").doClick(0);
            } else {
                Participante rivalAnterior = caso.partida.getRivalActual();
                controlVisible(caso.ventana, "Aceptar segunda fase").doClick(0);
                comprobar(caso.partida.getFase() == 2, "Aceptar debe comenzar la segunda fase");
                caso.pintar(null, "fase-dos");
                comprobar(caso.partida.getRivalActual() != rivalAnterior,
                        "La segunda fase debe cambiar el rival que representa la miniatura");
                comprobar(verificarCandidatosMiniatura(caso).equals(heredados),
                        "La nueva máquina debe conservar visualmente los descartes heredados");
                tarjetaVisible(caso.ventana, referencia.segundoSecreto.getId()).doClick(0);
                controlVisible(caso.ventana, "Arriesgar personaje").doClick(0);
                secretoEsperado = referencia.segundoSecreto.getNombre();
            }
            comprobar(caso.partida.getEstado() == EstadoPartida.FINALIZADA, "Debe aparecer el resultado final");
            caso.pintar(capturas, "05-resultado");
            comprobar(textoVisible(caso.ventana).contains(secretoEsperado),
                    "El resultado debe mostrar el secreto rival final");
            comprobar(caso.servicio.consultarUsuario("Ana").getPartidasTotales() == 1,
                    "La interacción gráfica debe registrar una sola partida");
            controlVisible(caso.ventana, "Nueva partida").doClick(0);
            caso.pintar(null, "regreso");
            controlVisible(caso.ventana, "Jugar contra Máquina 1").doClick(0);
            caso.pintar(null, "nueva-seleccion");
            int nuevoSecreto = caso.partida.getPersonajes().get(0).getId();
            tarjetaVisible(caso.ventana, nuevoSecreto).doClick(0);
            controlVisible(caso.ventana, "Elegir personaje").doClick(0);
            caso.pintar(null, "nueva-partida");
            comprobar(verificarCandidatosMiniatura(caso).size() == 23,
                    "Volver a jugar debe quitar los descartes de la miniatura anterior");
        } catch (Exception ex) {
            throw new AssertionError("Falló el flujo humano de " + modo, ex);
        } finally {
            caso.ventana.cerrar();
        }
    }

    private static void probarEspectador(Path capturas) {
        Escenario caso = new Escenario();
        try {
            caso.controlador.abrir();
            controlVisible(caso.ventana, "Observar partida").doClick(0);
            caso.pintar(null, "espectador-inicio");
            controlVisible(caso.ventana, "Pausar").doClick(0);
            comprobar(caso.reloj.pendiente == null, "Pausar debe cancelar el avance automático");
            controlVisible(caso.ventana, "Siguiente turno").doClick(0);
            controlVisible(caso.ventana, "Siguiente turno").doClick(0);
            comprobar(caso.partida.getRonda() == 2, "Dos pasos deben resolver una ronda");
            String texto = textoVisible(caso.ventana);
            comprobar(texto.contains("Sorteo:") && texto.contains("diferencia"),
                    "El razonamiento debe estar expuesto sin otro control");
            comprobar(componentes(caso.ventana, TarjetaPersonaje.class).stream()
                    .filter(FlujoSwingRegressionTest::visible).count() >= 46,
                    "El espectador debe disponer de dos tableros completos");
            caso.pintar(capturas, "06-espectador");
            componentes(caso.ventana, JComboBox.class).get(0).setSelectedIndex(2);
            comprobar(caso.reloj.pendiente == null, "Cambiar velocidad debe conservar la pausa");
            controlVisible(caso.ventana, "Continuar").doClick(0);
            comprobar(caso.reloj.pendiente != null, "Reanudar debe programar la máquina");
            comprobar(caso.reloj.demora == 450, "El selector debe aplicar la velocidad elegida");
            int turnos = 0;
            while (caso.partida.getEstado() == EstadoPartida.EN_CURSO && turnos++ < 100) caso.reloj.avanzar();
            comprobar(caso.partida.getEstado() == EstadoPartida.FINALIZADA, "Las máquinas deben terminar la partida");
            caso.pintar(capturas, "07-resultado-espectador");
            comprobar(caso.servicio.consultarMaquinas().getPartidasTotales() == 1,
                    "El resultado espectador debe registrarse en el marcador global");
        } catch (Exception ex) {
            throw new AssertionError("Falló el flujo espectador", ex);
        } finally {
            caso.ventana.cerrar();
        }
    }

    private static TarjetaPersonaje tarjetaVisible(Container raiz, int id) {
        return componentes(raiz, TarjetaPersonaje.class).stream()
                .filter(tarjeta -> tarjeta.id() == id && visible(tarjeta)).findFirst()
                .orElseThrow(() -> new AssertionError("Falta la carta " + id));
    }

    private static Set<Integer> verificarCandidatosMiniatura(Escenario caso) {
        PanelMiniTablero tablero = componentes(caso.ventana, PanelMiniTablero.class).stream()
                .filter(FlujoSwingRegressionTest::visible).findFirst()
                .orElseThrow(() -> new AssertionError("Falta la miniatura visible del rival"));
        var miniaturas = componentes(tablero, PanelMiniTablero.Miniatura.class);
        comprobar(miniaturas.stream().map(PanelMiniTablero.Miniatura::id).toList()
                        .equals(caso.partida.getPersonajes().stream().map(Personaje::getId).toList()),
                "La miniatura debe representar el mazo actual en su orden original");
        Set<Integer> activos = miniaturas.stream().filter(miniatura -> !miniatura.descartado())
                .map(PanelMiniTablero.Miniatura::id).collect(Collectors.toSet());
        Set<Integer> esperados = caso.partida.getCandidatos(caso.partida.getRivalActual()).stream()
                .map(Personaje::getId).collect(Collectors.toSet());
        comprobar(activos.equals(esperados), "La miniatura debe mostrar los candidatos del rival actual");
        comprobar(textoVisible(tablero).contains(caso.partida.getRivalActual().getNombre()),
                "La miniatura debe identificar a la máquina que conserva esos candidatos");
        return activos;
    }

    private static void verificarDistribucionLateral(VentanaPrincipal ventana) {
        Container contenido = ventana.getContentPane();
        PanelMiniTablero miniatura = componentes(ventana, PanelMiniTablero.class).stream()
                .filter(FlujoSwingRegressionTest::visible).findFirst().orElseThrow();
        PanelSecreto secreto = componentes(ventana, PanelSecreto.class).stream()
                .filter(FlujoSwingRegressionTest::visible).findFirst().orElseThrow();
        PanelHistorial historial = componentes(ventana, PanelHistorial.class).stream()
                .filter(FlujoSwingRegressionTest::visible).findFirst().orElseThrow();
        Rectangle limitesSecreto = limitesEn(secreto, contenido);
        Rectangle limitesMiniatura = limitesEn(miniatura, contenido);
        Rectangle limitesHistorial = limitesEn(historial, contenido);
        Rectangle limitesContenido = new Rectangle(0, 0, contenido.getWidth(), contenido.getHeight());
        comprobar(limitesSecreto.y + limitesSecreto.height <= limitesMiniatura.y
                        && limitesMiniatura.y + limitesMiniatura.height <= limitesHistorial.y,
                "La columna debe conservar el orden secreto, miniatura e historial sin superposición");
        comprobar(limitesContenido.contains(limitesHistorial) && limitesContenido.contains(limitesMiniatura),
                "La miniatura y el historial deben quedar dentro de la ventana al reducir su tamaño");
        for (PanelMiniTablero.Miniatura carta : componentes(miniatura, PanelMiniTablero.Miniatura.class)) {
            Rectangle limitesCarta = limitesEn(carta, miniatura);
            comprobar(carta.getWidth() > 0 && carta.getHeight() > 0
                            && new Rectangle(0, 0, miniatura.getWidth(), miniatura.getHeight()).contains(limitesCarta),
                    "Las 23 miniaturas deben poder verse completas en ambos tamaños de ventana");
        }
        JScrollPane scroll = componentes(historial, JScrollPane.class).get(0);
        comprobar(scroll.getViewport().getHeight() >= 70,
                "El historial compacto debe conservar al menos 70 px legibles; tiene "
                        + scroll.getViewport().getHeight() + " px");
        JCheckBox razonamiento = componentes(historial, JCheckBox.class).get(0);
        comprobar(visible(razonamiento)
                        && new Rectangle(0, 0, historial.getWidth(), historial.getHeight())
                                .contains(limitesEn(razonamiento, historial)),
                "El control de razonamiento debe permanecer disponible debajo del historial");
    }

    private static Rectangle limitesEn(Component componente, Container destino) {
        return SwingUtilities.convertRectangle(componente.getParent(), componente.getBounds(), destino);
    }

    private static AbstractButton controlVisible(Container raiz, String texto) {
        return componentes(raiz, AbstractButton.class).stream()
                .filter(boton -> texto.equals(boton.getText()) && visible(boton)).findFirst()
                .orElseThrow(() -> new AssertionError("Falta el botón visible: " + texto));
    }

    private static boolean visible(Component componente) {
        // JFrame permanece oculto; se respeta la visibilidad de sus pantallas internas.
        for (Component actual = componente; actual != null && !(actual instanceof VentanaPrincipal);
                actual = actual.getParent()) {
            if (!actual.isVisible()) return false;
        }
        return true;
    }

    private static String textoVisible(Container raiz) {
        StringBuilder texto = new StringBuilder();
        for (JLabel etiqueta : componentes(raiz, JLabel.class)) {
            if (visible(etiqueta)) texto.append(etiqueta.getText()).append('\n');
        }
        for (JTextArea area : componentes(raiz, JTextArea.class)) {
            if (visible(area)) texto.append(area.getText()).append('\n');
        }
        return texto.toString();
    }

    private static final class Reloj implements ControladorJuego.RelojTurnos {
        private Runnable pendiente;
        private int demora;
        @Override public void programar(int nuevaDemora, Runnable accion) { demora = nuevaDemora; pendiente = accion; }
        @Override public void cancelar() { pendiente = null; }
        void avanzar() {
            Runnable accion = pendiente;
            comprobar(accion != null, "Falta un turno programado");
            pendiente = null;
            accion.run();
        }
    }

    private static final class Memoria implements IRepositorioEstadisticas {
        private Properties datos = new Properties();
        private boolean fallarGuardado;
        @Override public Properties cargar() { return (Properties) datos.clone(); }
        @Override public void guardar(Properties nuevos) throws java.io.IOException {
            if (fallarGuardado) {
                fallarGuardado = false;
                throw new java.io.IOException("Fallo de guardado simulado para comprobar el reintento");
            }
            datos = (Properties) nuevos.clone();
        }
    }

    private static final class Escenario {
        final VentanaPrincipal ventana = new VentanaPrincipal();
        final Partida partida = new Partida(new Random(9),
                new PartidaPrueba.AzarFijo(.99, 0), new PartidaPrueba.AzarFijo(.99, 0));
        final Memoria repositorio = new Memoria();
        final ServicioEstadisticas servicio = new ServicioEstadisticas(repositorio);
        final Reloj reloj = new Reloj();
        final ControladorJuego controlador = new ControladorJuego(partida, servicio, ventana,
                reloj, Runnable::run, Runnable::run);
        Escenario() { ventana.conectar(controlador); }

        void pintar(Path carpeta, String nombre) {
            pintar(carpeta, nombre, 1348, 730);
        }

        void pintar(Path carpeta, String nombre, int ancho, int alto) {
            Container contenido = ventana.getContentPane();
            contenido.setSize(ancho, alto);
            for (int i = 0; i < 3; i++) distribuir(contenido);
            BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
            Graphics2D grafico = imagen.createGraphics();
            contenido.printAll(grafico);
            grafico.dispose();
            if (carpeta != null) {
                try { ImageIO.write(imagen, "png", carpeta.resolve(nombre + ".png").toFile()); }
                catch (java.io.IOException ex) { throw new AssertionError("No se pudo guardar la captura", ex); }
            }
        }
    }
}
