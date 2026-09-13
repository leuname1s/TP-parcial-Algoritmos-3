package GUI;

import datos.Personaje;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Vista informativa de los candidatos con los que el rival busca el secreto humano. */
@SuppressWarnings("serial") // Los componentes de presentación no se serializan.
final class PanelMiniTablero extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel titulo = new JLabel();
    private final JLabel contador = new JLabel();
    private final JPanel rejilla = Tema.panel(new GridLayout(0, 6, 4, 4));
    private final Map<Integer, Miniatura> miniaturas = new LinkedHashMap<>();
    private List<Personaje> personajes = List.of();

    PanelMiniTablero() {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        titulo.setFont(Tema.FUENTE.deriveFont(Font.BOLD, 16f));
        contador.setFont(Tema.FUENTE.deriveFont(12f));
        contador.setForeground(Tema.SUAVE);
        JPanel cabecera = Tema.panel(new BorderLayout(0, 2));
        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(contador, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);
        JPanel centro = Tema.panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.add(rejilla);
        add(centro, BorderLayout.CENTER);
        getAccessibleContext().setAccessibleDescription("Personajes que la máquina considera para descubrir tu secreto.");
    }

    void actualizar(String nombreRival, List<Personaje> nuevos, Set<Integer> candidatos) {
        if (!personajes.equals(nuevos)) {
            personajes = List.copyOf(nuevos);
            miniaturas.clear();
            rejilla.removeAll();
            for (Personaje personaje : personajes) {
                Miniatura miniatura = new Miniatura(personaje);
                miniaturas.put(personaje.getId(), miniatura);
                rejilla.add(miniatura);
            }
            revalidate();
        }
        int restantes = 0;
        for (Miniatura miniatura : miniaturas.values()) {
            boolean descartado = !candidatos.contains(miniatura.id());
            miniatura.actualizar(descartado);
            if (!descartado) restantes++;
        }
        titulo.setText("Candidatos de " + nombreRival);
        contador.setText("En juego: " + restantes + " / " + personajes.size()
                + " · Descartados: " + (personajes.size() - restantes));
        getAccessibleContext().setAccessibleName(titulo.getText());
        repaint();
    }

    static final class Miniatura extends JLabel {
        private static final long serialVersionUID = 1L;
        private final Personaje personaje;
        private final ImageIcon imagen;
        private final ImageIcon imagenDescartada;
        private boolean descartado;

        Miniatura(Personaje personaje) {
            this.personaje = personaje;
            imagen = ImagenesPersonajes.obtener(personaje.getId(), 36);
            imagenDescartada = imagen.getIconWidth() > 0
                    ? new ImageIcon(GrayFilter.createDisabledImage(imagen.getImage())) : imagen;
            setPreferredSize(new Dimension(44, 44));
            setMinimumSize(getPreferredSize());
            getAccessibleContext().setAccessibleName(personaje.getNombre() + ", ID " + personaje.getId());
        }

        int id() { return personaje.getId(); }
        boolean descartado() { return descartado; }

        void actualizar(boolean nuevoDescartado) {
            descartado = nuevoDescartado;
            String estado = descartado ? "Descartado por la máquina" : "Candidato de la máquina";
            setToolTipText(TarjetaPersonaje.atributos(personaje).replace("</html>", "<br><b>" + estado + "</b></html>"));
            getAccessibleContext().setAccessibleDescription(TarjetaPersonaje.atributosTexto(personaje) + ". " + estado + ".");
            repaint();
        }

        @Override protected void paintComponent(Graphics grafico) {
            Graphics2D g = (Graphics2D) grafico.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(descartado ? new Color(0xeef0f3) : Color.WHITE);
            g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            g.setColor(Tema.BORDE);
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            if (descartado) g.setComposite(AlphaComposite.SrcOver.derive(.4f));
            if (imagen.getIconWidth() > 0) {
                (descartado ? imagenDescartada : imagen).paintIcon(this, g, (getWidth() - 36) / 2, (getHeight() - 36) / 2);
            } else {
                g.setColor(Tema.TEXTO);
                g.setFont(Tema.FUENTE.deriveFont(Font.BOLD, 20f));
                String inicial = personaje.getNombre().substring(0, 1);
                g.drawString(inicial, (getWidth() - g.getFontMetrics().stringWidth(inicial)) / 2, 29);
            }
            if (descartado) {
                g.setComposite(AlphaComposite.SrcOver);
                g.setColor(Tema.SUAVE);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(12, 12, getWidth() - 12, getHeight() - 12);
                g.drawLine(getWidth() - 12, 12, 12, getHeight() - 12);
            }
            g.dispose();
        }
    }
}
