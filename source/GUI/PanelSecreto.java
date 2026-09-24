package GUI;

import datos.Personaje;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class PanelSecreto extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel imagen = new JLabel();
    private final JLabel texto = new JLabel();
    private final String titulo;

    PanelSecreto(String titulo) {
        super(new BorderLayout(10, 5));
        this.titulo = titulo;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        add(imagen, BorderLayout.WEST);
        add(texto, BorderLayout.CENTER);
        actualizar(null);
    }

    void actualizar(Personaje personaje) {
        imagen.setIcon(personaje == null ? null : ImagenesPersonajes.obtener(personaje.getId(), 64));
        texto.setText("<html><span style='color:#625b69'>" + titulo + "</span><br><b>"
                + (personaje == null ? "Por elegir" : Tema.html(personaje.getNombre())) + "</b>"
                + (personaje == null ? "" : " · ID " + personaje.getId()) + "</html>");
        String ayuda = personaje == null ? null : TarjetaPersonaje.atributos(personaje);
        setToolTipText(ayuda);
        imagen.setToolTipText(ayuda);
        texto.setToolTipText(ayuda);
        getAccessibleContext().setAccessibleDescription(personaje == null ? titulo : titulo + ": "
                + personaje.getNombre() + ". " + TarjetaPersonaje.atributosTexto(personaje));
    }
}
