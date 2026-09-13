package GUI;

import Controladores.TurnoRegistrado;
import datos.ModoJuego;
import datos.Personaje;
import datos.Pregunta;
import defaults.CatalogoPersonajes;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JScrollPane;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import soporte.PartidaPrueba;
import static soporte.PartidaPrueba.comprobar;

/** Contratos de interacción de las cartas; se ejecutan en EDT sin mostrar ventanas. */
public final class InterfazSwingRegressionTest {
    private InterfazSwingRegressionTest() { }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Tema.instalar();
            verificarRecursos();
            verificarTablero();
            verificarMiniTablero();
            verificarPreguntas();
            verificarHistorial();
        });
        System.out.println("PASS: recursos, cartas, mini tablero, preguntas y razonamiento visible en Swing");
    }

    private static void verificarPreguntas() {
        List<Pregunta> enviadas = new ArrayList<>();
        PanelPreguntas panel = new PanelPreguntas(enviadas::add);
        panel.actualizar(1, Set.of(), true);
        boton(panel, "Pelo").doClick(0);
        boton(panel, "Rubio").doClick(0);
        comprobar(enviadas.isEmpty(), "Elegir categoría y pregunta no debe ejecutar un turno");
        boton(panel, "Preguntar").doClick(0);
        comprobar(enviadas.equals(List.of(Pregunta.PELO_RUBIO)), "Enviar ejecuta la pregunta elegida");
        panel.actualizar(1, Set.of(Pregunta.PELO_RUBIO), true);
        comprobar(!boton(panel, "Rubio (usada)").isEnabled(), "La pregunta repetida debe quedar deshabilitada");
        comprobar(!boton(panel, "Preguntar").isEnabled(), "No se puede reenviar la pregunta usada");
        panel.actualizar(2, Set.of(), true);
        comprobar(boton(panel, "Rubio").isEnabled(), "La segunda fase permite volver a preguntar");
        comprobar(!boton(panel, "Preguntar").isEnabled(), "La segunda fase reinicia la elección");
        comprobar(componentes(panel, JRadioButton.class).size() == Pregunta.values().length,
                "Las categorías deben incluir todas las preguntas del motor");
        panel.actualizar(2, Set.of(), false);
        comprobar(componentes(panel, JRadioButton.class).stream().noneMatch(Component::isEnabled),
                "No se envían preguntas durante el turno rival");
        boton(panel, "Preguntar").doClick(0);
        comprobar(enviadas.size() == 1, "Los controles deshabilitados no producen acciones");
    }

    private static void verificarHistorial() {
        PartidaPrueba prueba = new PartidaPrueba(ModoJuego.JUGADOR_VS_MAQUINA_1, true,
                new PartidaPrueba.AzarFijo(.99, 0), new PartidaPrueba.AzarFijo(.99, 0));
        prueba.fallarJugador();
        TurnoRegistrado turno = new TurnoRegistrado(1, 1, prueba.partida.ejecutarTurnoMaquina());
        comprobar(turno.resultado().getDiagnostico() != null, "Escenario con diagnóstico de máquina");
        PanelHistorial humano = new PanelHistorial(false);
        humano.actualizar(List.of(turno));
        JTextArea resumen = componentes(humano, JTextArea.class).get(0);
        comprobar(!resumen.getText().contains("Sorteo:"), "El humano empieza con historial resumido");
        componentes(humano, JCheckBox.class).get(0).doClick(0);
        comprobar(resumen.getText().contains("Sorteo:"), "El humano puede ampliar el razonamiento");
        PanelHistorial espectador = new PanelHistorial(true);
        espectador.actualizar(List.of(turno));
        String completo = componentes(espectador, JTextArea.class).get(0).getText();
        comprobar(completo.contains("Sorteo:") && completo.contains("Evaluación de preguntas"),
                "El espectador siempre recibe razonamiento y comparaciones");
        comprobar(componentes(espectador, JCheckBox.class).stream().noneMatch(Component::isVisible),
                "El razonamiento de espectador no debe requerir desplegarlo");
        comprobar(!completo.contains("elegir al azar entre preguntas útiles"),
                "Máquina 1 maximiza la diferencia: solo el desempate es aleatorio");
    }

    static AbstractButton boton(Container raiz, String texto) {
        return componentes(raiz, AbstractButton.class).stream()
                .filter(boton -> texto.equals(boton.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el control: " + texto));
    }

    private static void verificarRecursos() {
        for (Personaje personaje : CatalogoPersonajes.crearCatalogo()) {
            comprobar(ImagenesPersonajes.obtener(personaje.getId(), 100).getIconWidth() == 100,
                    "Falta el sprite del ID " + personaje.getId());
        }
        comprobar(ImagenesPersonajes.obtener(999, 100).getIconWidth() < 0,
                "Un recurso ausente debe permitir el placeholder");
    }

    private static void verificarTablero() {
        List<Personaje> personajes = new ArrayList<>();
        for (Personaje personaje : CatalogoPersonajes.crearMazo(new Random(9))) personajes.add(personaje);
        Set<Integer> candidatos = new HashSet<>();
        personajes.forEach(personaje -> candidatos.add(personaje.getId()));
        AtomicInteger elegido = new AtomicInteger(-1);
        PanelTablero tablero = new PanelTablero("Tu tablero", elegido::set);
        tablero.actualizar("Tu tablero", personajes, candidatos, true);
        tablero.setSize(720, 450);
        distribuir(tablero);
        List<TarjetaPersonaje> tarjetas = componentes(tablero, TarjetaPersonaje.class);
        comprobar(tarjetas.size() == 23, "Deben conservarse las 23 cartas del mazo");
        TarjetaPersonaje primera = tarjetas.get(0);
        comprobar(primera.getToolTipText().contains(personajes.get(0).getNombre()), "Hover con identidad");
        comprobar(primera.getAccessibleContext().getAccessibleDescription().contains("lentes"),
                "Los atributos deben ser accesibles sin depender del dibujo");
        primera.doClick(0);
        comprobar(elegido.get() == primera.id() && tablero.seleccion() == primera.id(),
                "Seleccionar comunica el ID estable, no el índice de la carta");
        JScrollPane scroll = componentes(tablero, JScrollPane.class).get(0);
        scroll.getVerticalScrollBar().setValue(100);
        int desplazamiento = scroll.getVerticalScrollBar().getValue();
        tablero.actualizar("Tu tablero", personajes, candidatos, false);
        comprobar(tablero.seleccion() == primera.id(), "Actualizar el turno debe conservar la selección");
        comprobar(scroll.getVerticalScrollBar().getValue() == desplazamiento,
                "Actualizar el turno no debe devolver el tablero al inicio");
        tarjetas.get(1).doClick(0);
        comprobar(elegido.get() == primera.id(), "El turno rival impide seleccionar otra carta");
        candidatos.remove(primera.id());
        tablero.actualizar("Tu tablero", personajes, candidatos, true);
        comprobar(tablero.seleccion() == null, "Descartar la selección debe desactivar el intento");
        elegido.set(-1);
        primera.doClick(0);
        comprobar(elegido.get() == -1, "Una carta descartada no debe seleccionar un intento");
        comprobar(primera.getToolTipText() != null, "Descartar conserva la consulta de atributos");
        comprobar(componentes(tablero, TarjetaPersonaje.class).get(0) == primera,
                "Descartar conserva posición e identidad visual");
        comprobar(scroll.getVerticalScrollBar().getMaximum() > scroll.getViewport().getHeight(),
                "Las filas restantes deben ser alcanzables con desplazamiento");
        tablero.setSize(330, 450);
        distribuir(tablero);
        BufferedImage imagen = new BufferedImage(330, 450, BufferedImage.TYPE_INT_ARGB);
        Graphics2D grafico = imagen.createGraphics();
        tablero.printAll(grafico);
        grafico.dispose();
        comprobar(scroll.getViewport().getView().getWidth() <= scroll.getViewport().getWidth(),
                "El tablero angosto debe evitar desplazamiento horizontal");
        tablero.cancelarEfectos();
    }

    private static void verificarMiniTablero() {
        List<Personaje> personajes = new ArrayList<>();
        for (Personaje personaje : CatalogoPersonajes.crearMazo(new Random(9))) personajes.add(personaje);
        Set<Integer> candidatos = new HashSet<>();
        personajes.forEach(personaje -> candidatos.add(personaje.getId()));
        PanelMiniTablero tablero = new PanelMiniTablero();
        tablero.actualizar("Máquina 1", personajes, candidatos);
        List<PanelMiniTablero.Miniatura> miniaturas = componentes(tablero, PanelMiniTablero.Miniatura.class);
        comprobar(miniaturas.stream().map(PanelMiniTablero.Miniatura::id).toList()
                        .equals(personajes.stream().map(Personaje::getId).toList()),
                "La miniatura debe conservar las 23 identidades y el orden del mazo");
        comprobar(miniaturas.stream().noneMatch(PanelMiniTablero.Miniatura::descartado),
                "Al comenzar todos los candidatos deben estar activos");
        comprobar(componentes(tablero, AbstractButton.class).isEmpty(),
                "La miniatura es informativa y no ofrece acciones sobre las cartas");
        PanelMiniTablero.Miniatura primera = miniaturas.get(0);
        Personaje personaje = personajes.get(0);
        comprobar(primera.getToolTipText().contains(personaje.getNombre())
                        && primera.getToolTipText().contains("ID " + personaje.getId()),
                "El hover debe identificar al personaje por nombre e ID");
        comprobar(primera.getAccessibleContext().getAccessibleDescription().contains("lentes"),
                "La miniatura debe describir los atributos sin depender de su tamaño");
        candidatos.remove(primera.id());
        tablero.actualizar("Máquina 1", personajes, candidatos);
        comprobar(primera.descartado(), "El descarte debe reflejarse en la miniatura correspondiente");
        comprobar(componentes(tablero, PanelMiniTablero.Miniatura.class).equals(miniaturas),
                "Descartar debe conservar las miniaturas y sus posiciones");
        comprobar(primera.getToolTipText().toLowerCase(java.util.Locale.ROOT).contains("descartad"),
                "El estado descartado debe poder consultarse en el hover");
        candidatos.add(primera.id());
        tablero.actualizar("Máquina 2", personajes, candidatos);
        comprobar(!primera.descartado(), "Reiniciar con el mismo mazo debe restaurar los candidatos");
        comprobar(componentes(tablero, javax.swing.JLabel.class).stream()
                        .anyMatch(etiqueta -> etiqueta.getText() != null && etiqueta.getText().contains("Máquina 2")),
                "El encabezado debe identificar al rival actual");

        List<Personaje> nuevos = new ArrayList<>();
        for (Personaje nuevo : CatalogoPersonajes.crearMazo(new Random(11))) nuevos.add(nuevo);
        Set<Integer> nuevosCandidatos = new HashSet<>();
        nuevos.forEach(nuevo -> nuevosCandidatos.add(nuevo.getId()));
        tablero.actualizar("Máquina 2", nuevos, nuevosCandidatos);
        List<PanelMiniTablero.Miniatura> reemplazos = componentes(tablero, PanelMiniTablero.Miniatura.class);
        comprobar(reemplazos.stream().map(PanelMiniTablero.Miniatura::id).toList()
                        .equals(nuevos.stream().map(Personaje::getId).toList()),
                "Cambiar de partida debe mostrar el nuevo mazo sin identidades anteriores");
        comprobar(reemplazos.stream().noneMatch(PanelMiniTablero.Miniatura::descartado),
                "El nuevo mazo debe comenzar sin los descartes de la partida anterior");
    }

    static void distribuir(Container raiz) {
        raiz.doLayout();
        for (Component componente : raiz.getComponents()) {
            if (componente instanceof Container contenedor) distribuir(contenedor);
        }
    }

    static <T extends Component> List<T> componentes(Container raiz, Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Component componente : raiz.getComponents()) {
            if (tipo.isInstance(componente)) encontrados.add(tipo.cast(componente));
            if (componente instanceof Container contenedor) encontrados.addAll(componentes(contenedor, tipo));
        }
        return encontrados;
    }
}
