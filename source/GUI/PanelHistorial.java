package GUI;

import Controladores.TurnoRegistrado;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** El espectador recibe siempre el diagnóstico completo de ambas máquinas. */
@SuppressWarnings("serial") // Los componentes de presentación no se serializan.
final class PanelHistorial extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextArea contenido = new JTextArea();
    private final JCheckBox detalle = new JCheckBox("Mostrar razonamiento de la máquina");
    private final JScrollPane scroll = new JScrollPane(contenido);
    private final JLabel titulo = Tema.titulo("");
    private boolean espectador;
    private List<TurnoRegistrado> turnos = List.of();

    PanelHistorial(boolean espectador) {
        super(new BorderLayout(0, 5));
        this.espectador = espectador;
        setOpaque(false);
        titulo.setText(espectador ? "Historial y razonamiento de ambas máquinas" : "Historial de la partida");
        add(titulo, BorderLayout.NORTH);
        contenido.setEditable(false);
        contenido.setLineWrap(true);
        contenido.setWrapStyleWord(true);
        contenido.setBackground(Color.WHITE);
        contenido.setForeground(Tema.TEXTO);
        contenido.setFont(Tema.FUENTE.deriveFont(13f));
        contenido.setMargin(new java.awt.Insets(10, 10, 10, 10));
        contenido.setText("Todavía no hay acciones.");
        contenido.getAccessibleContext().setAccessibleName("Historial de acciones y decisiones");
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scroll.setMinimumSize(new Dimension(160, 90));
        add(scroll, BorderLayout.CENTER);
        detalle.setOpaque(false);
        detalle.setFont(Tema.FUENTE.deriveFont(12f));
        detalle.addActionListener(evento -> presentar());
        add(detalle, BorderLayout.SOUTH);
        detalle.setVisible(!espectador);
    }

    void configurarEspectador(boolean nuevoEspectador) {
        if (espectador == nuevoEspectador) return;
        espectador = nuevoEspectador;
        titulo.setText(espectador ? "Historial y razonamiento de ambas máquinas" : "Historial de la partida");
        detalle.setVisible(!espectador);
        presentar();
    }

    void actualizar(List<TurnoRegistrado> nuevos) {
        if (turnos.equals(nuevos)) return;
        turnos = List.copyOf(nuevos);
        presentar();
    }

    private void presentar() {
        JScrollBar barra = scroll.getVerticalScrollBar();
        int posicion = barra.getValue();
        boolean alFinal = posicion + barra.getVisibleAmount() >= barra.getMaximum() - 8;
        StringBuilder texto = new StringBuilder();
        for (TurnoRegistrado turno : turnos) {
            if (!texto.isEmpty()) texto.append("\n\n");
            texto.append(TextoPartida.resumen(turno));
            if ((espectador || detalle.isSelected()) && turno.resultado().getDiagnostico() != null) {
                texto.append("\n\n").append(TextoPartida.razonamiento(turno.resultado().getDiagnostico()));
            }
        }
        contenido.setText(texto.isEmpty() ? "Todavía no hay acciones." : texto.toString());
        contenido.setCaretPosition(0);
        SwingUtilities.invokeLater(() -> barra.setValue(alFinal ? barra.getMaximum() : posicion));
    }
}
