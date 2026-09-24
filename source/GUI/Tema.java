package GUI;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

final class Tema {
    static final Color FONDO = new Color(0xe4f4ff);
    static final Color CELESTE = new Color(0xa6e1ff);
    static final Color AMARILLO = new Color(0xfff2b2);
    static final Color ROSA = new Color(0xb3747e);
    static final Color TEXTO = new Color(0x302b35);
    static final Color SUAVE = new Color(0x625b69);
    static final Color BORDE = new Color(0xded4d5);
    static final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 14);

    private Tema() { }

    static void instalar() {
        for (Object clave : UIManager.getDefaults().keySet().toArray()) {
            if (clave.toString().endsWith(".font")) UIManager.put(clave, FUENTE);
        }
        UIManager.put("ToolTip.background", AMARILLO);
        UIManager.put("ToolTip.foreground", TEXTO);
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.foreground", TEXTO);
    }

    static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(FONDO);
        return panel;
    }

    static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE.deriveFont(Font.BOLD, 18f));
        return etiqueta;
    }

    static JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setBackground(CELESTE);
        boton.setForeground(TEXTO);
        boton.setFocusPainted(true);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x7aabc5)),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        boton.addActionListener(evento -> accion.run());
        return boton;
    }

    static void margen(JComponent componente, int margen) {
        componente.setBorder(BorderFactory.createEmptyBorder(margen, margen, margen, margen));
    }

    static String html(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
