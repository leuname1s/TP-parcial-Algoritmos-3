package GUI;

import datos.Personaje;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;

@SuppressWarnings("serial") // Los componentes de presentación no se serializan.
final class TarjetaPersonaje extends JButton {
    private static final long serialVersionUID = 1L;
    private final Personaje personaje;
    private final ImageIcon imagen;
    private final ImageIcon imagenDescartada;
    private boolean descartado;
    private boolean elegible;
    private boolean seleccionado;
    private float efecto;
    private Timer animacion;
    private Popup ayudaFoco;

    TarjetaPersonaje(Personaje personaje, Runnable accion) {
        this.personaje = personaje;
        imagen = ImagenesPersonajes.obtener(personaje.getId(), 100);
        imagenDescartada = imagen.getIconWidth() > 0
                ? new ImageIcon(javax.swing.GrayFilter.createDisabledImage(imagen.getImage())) : imagen;
        setPreferredSize(new Dimension(130, 154));
        setMinimumSize(getPreferredSize());
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setToolTipText(atributos(personaje));
        getAccessibleContext().setAccessibleName(personaje.getNombre() + ", ID " + personaje.getId());
        getAccessibleContext().setAccessibleDescription(atributosTexto(personaje));
        addActionListener(evento -> { if (elegible) accion.run(); });
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent evento) { mostrarAyuda(); repaint(); }
            @Override public void focusLost(FocusEvent evento) { ocultarAyuda(); repaint(); }
        });
    }

    int id() { return personaje.getId(); }
    Personaje personaje() { return personaje; }

    void actualizar(boolean nuevoDescartado, boolean nuevoElegible, boolean nuevaSeleccion) {
        boolean cambio = descartado != nuevoDescartado || seleccionado != nuevaSeleccion;
        descartado = nuevoDescartado;
        elegible = nuevoElegible && !descartado;
        seleccionado = nuevaSeleccion;
        getAccessibleContext().setAccessibleDescription(atributosTexto(personaje)
                + (descartado ? ". Descartado." : seleccionado ? ". Seleccionado." : ""));
        if (cambio && isShowing()) animar();
        repaint();
    }

    private void animar() {
        if (animacion != null) animacion.stop();
        efecto = 1f;
        animacion = new Timer(30, evento -> {
            efecto = Math.max(0f, efecto - .15f);
            repaint();
            if (efecto == 0f) ((Timer) evento.getSource()).stop();
        });
        animacion.start();
    }

    void cancelarEfectos() {
        if (animacion != null) animacion.stop();
        efecto = 0;
        ocultarAyuda();
    }

    @Override public void removeNotify() {
        cancelarEfectos();
        super.removeNotify();
    }

    private void mostrarAyuda() {
        if (!isShowing()) return;
        ocultarAyuda();
        JLabel texto = new JLabel(atributos(personaje));
        texto.setOpaque(true);
        texto.setBackground(Tema.AMARILLO);
        texto.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.ROSA),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        Point posicion = getLocationOnScreen();
        Dimension tamano = texto.getPreferredSize();
        java.awt.Rectangle pantalla = getGraphicsConfiguration().getBounds();
        int x = Math.min(posicion.x + getWidth(), pantalla.x + pantalla.width - tamano.width - 8);
        int y = Math.min(posicion.y, pantalla.y + pantalla.height - tamano.height - 8);
        ayudaFoco = PopupFactory.getSharedInstance().getPopup(this, texto, x, y);
        ayudaFoco.show();
    }

    private void ocultarAyuda() {
        if (ayudaFoco != null) { ayudaFoco.hide(); ayudaFoco = null; }
    }

    @Override protected void paintComponent(Graphics grafico) {
        Graphics2D g = (Graphics2D) grafico.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fondo = seleccionado ? Tema.AMARILLO : descartado ? new Color(0xeef0f3) : Color.WHITE;
        g.setColor(fondo);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        g.setColor(seleccionado || hasFocus() ? Tema.ROSA : Tema.BORDE);
        g.setStroke(new BasicStroke(seleccionado || hasFocus() ? 2f : 1f));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        if (descartado) g.setComposite(AlphaComposite.SrcOver.derive(.42f));
        if (imagen.getIconWidth() > 0) (descartado ? imagenDescartada : imagen).paintIcon(this, g, (getWidth() - 100) / 2, 6);
        else {
            g.setColor(Tema.CELESTE);
            g.fillRoundRect((getWidth() - 90) / 2, 10, 90, 90, 12, 12);
            g.setFont(Tema.FUENTE.deriveFont(Font.BOLD, 36f));
            g.setColor(Tema.TEXTO);
            String inicial = personaje.getNombre().substring(0, 1);
            g.drawString(inicial, (getWidth() - g.getFontMetrics().stringWidth(inicial)) / 2, 70);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(descartado ? Tema.SUAVE : Tema.TEXTO);
        g.setFont(Tema.FUENTE.deriveFont(Font.BOLD, 13f));
        String nombre = personaje.getNombre();
        while (g.getFontMetrics().stringWidth(nombre) > getWidth() - 12 && nombre.length() > 2) {
            nombre = nombre.substring(0, nombre.length() - 2) + "…";
        }
        g.drawString(nombre, (getWidth() - g.getFontMetrics().stringWidth(nombre)) / 2, 124);
        g.setFont(Tema.FUENTE.deriveFont(11f));
        String detalle = String.format(java.util.Locale.ROOT, "ID %02d", personaje.getId())
                + (descartado ? " · Descartado" : seleccionado ? " · Elegido" : "");
        g.drawString(detalle, (getWidth() - g.getFontMetrics().stringWidth(detalle)) / 2, 143);
        if (efecto > 0f) {
            g.setComposite(AlphaComposite.SrcOver.derive(efecto * .25f));
            g.setColor(Tema.CELESTE);
            g.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 12, 12);
        }
        g.dispose();
    }

    static String atributosTexto(Personaje p) {
        return (p.getGenero() == datos.Genero.FEMENINO ? "Mujer" : "Hombre")
                + ", " + switch (p.getColorPelo()) {
                    case PELADO -> "pelado";
                    case PELIRROJO -> "pelo pelirrojo";
                    case NEGRO -> "pelo negro";
                    case RUBIO -> "pelo rubio";
                } + ", " + (p.isTieneLentes() ? "con lentes" : "sin lentes")
                + ", " + (p.isTieneBarba() ? "con barba" : "sin barba")
                + ", " + (p.isLeFaltaUnDiente() ? "le falta un diente" : "dentadura completa");
    }

    static String atributos(Personaje p) {
        return "<html><b>" + Tema.html(p.getNombre()) + " · ID " + p.getId() + "</b><br>"
                + Tema.html(atributosTexto(p)).replace(", ", "<br>") + "</html>";
    }
}
