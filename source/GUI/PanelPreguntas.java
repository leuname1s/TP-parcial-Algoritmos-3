package GUI;

import datos.Pregunta;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

/** Elegir categoría o pregunta no consume turnos; solo lo hace el envío. */
@SuppressWarnings("serial") // Los componentes de presentación no se serializan.
final class PanelPreguntas extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Map<Pregunta, JRadioButton> opciones = new EnumMap<>(Pregunta.class);
    private final ButtonGroup grupoPreguntas = new ButtonGroup();
    private final JLabel seleccion = new JLabel("Elegí una pregunta.");
    private final JButton preguntar;
    private Pregunta elegida;
    private boolean habilitado;
    private int fase;
    private Set<Pregunta> realizadas = Set.of();

    PanelPreguntas(Consumer<Pregunta> enviar) {
        super(new BorderLayout(0, 3));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(4, 0, 0, 0)));
        JPanel cabecera = Tema.panel(new BorderLayout(0, 3));
        cabecera.add(Tema.titulo("Hacé una pregunta"), BorderLayout.NORTH);
        JPanel categorias = Tema.panel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        cabecera.add(categorias, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);
        CardLayout pantallas = new CardLayout();
        JPanel preguntas = Tema.panel(pantallas);
        add(preguntas, BorderLayout.CENTER);
        String[] nombres = {"Género", "Pelo", "Lentes", "Barba", "Diente"};
        Pregunta[][] valores = {
            {Pregunta.ES_FEMENINO, Pregunta.ES_MASCULINO},
            {Pregunta.ES_CALVO, Pregunta.PELO_NEGRO, Pregunta.PELO_RUBIO, Pregunta.PELO_PELIRROJO},
            {Pregunta.USA_LENTES}, {Pregunta.TIENE_BARBA}, {Pregunta.LE_FALTA_UN_DIENTE}
        };
        ButtonGroup grupoCategorias = new ButtonGroup();
        for (int i = 0; i < nombres.length; i++) {
            String nombre = nombres[i];
            JToggleButton categoria = new JToggleButton(nombre);
            categoria.setBackground(Tema.CELESTE);
            categoria.addActionListener(evento -> pantallas.show(preguntas, nombre));
            grupoCategorias.add(categoria);
            categorias.add(categoria);
            if (i == 0) categoria.setSelected(true);
            JPanel panel = Tema.panel(new FlowLayout(FlowLayout.LEADING, 4, 3));
            for (Pregunta pregunta : valores[i]) {
                JRadioButton opcion = new JRadioButton(textoCorto(pregunta));
                opcion.setOpaque(false);
                opcion.setToolTipText("¿" + pregunta.getTexto() + "?");
                opcion.addActionListener(evento -> {
                    elegida = pregunta;
                    refrescarEnvio();
                });
                grupoPreguntas.add(opcion);
                opciones.put(pregunta, opcion);
                panel.add(opcion);
            }
            preguntas.add(panel, nombre);
        }
        preguntar = Tema.boton("Preguntar", () -> {
            if (habilitado && elegida != null && !realizadas.contains(elegida)) enviar.accept(elegida);
        });
        JPanel pie = Tema.panel(new BorderLayout(8, 0));
        pie.add(seleccion, BorderLayout.CENTER);
        pie.add(preguntar, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
        refrescarEnvio();
    }

    void actualizar(int nuevaFase, Set<Pregunta> hechas, boolean puedePreguntar) {
        if (fase != nuevaFase || (elegida != null && hechas.contains(elegida))) {
            elegida = null;
            grupoPreguntas.clearSelection();
        }
        fase = nuevaFase;
        realizadas = Set.copyOf(hechas);
        habilitado = puedePreguntar;
        opciones.forEach((pregunta, opcion) -> {
            boolean usada = hechas.contains(pregunta);
            opcion.setEnabled(puedePreguntar && !usada);
            opcion.setText(textoCorto(pregunta) + (usada ? " (usada)" : ""));
        });
        refrescarEnvio();
    }

    void reiniciar() {
        elegida = null;
        fase = 0;
        grupoPreguntas.clearSelection();
        refrescarEnvio();
    }

    private void refrescarEnvio() {
        seleccion.setText(elegida == null ? "Elegí una pregunta." : "¿" + elegida.getTexto() + "?");
        preguntar.setEnabled(habilitado && elegida != null && !realizadas.contains(elegida));
    }

    private static String textoCorto(Pregunta pregunta) {
        return switch (pregunta) {
            case ES_FEMENINO -> "Mujer";
            case ES_MASCULINO -> "Hombre";
            case ES_CALVO -> "Pelado";
            case PELO_NEGRO -> "Negro";
            case PELO_RUBIO -> "Rubio";
            case PELO_PELIRROJO -> "Pelirrojo";
            case USA_LENTES -> "Usa lentes";
            case TIENE_BARBA -> "Tiene barba";
            case LE_FALTA_UN_DIENTE -> "Le falta un diente";
        };
    }
}
