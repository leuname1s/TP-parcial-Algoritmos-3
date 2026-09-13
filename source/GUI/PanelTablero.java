package GUI;

import datos.Personaje;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

@SuppressWarnings("serial") // Los componentes de presentación no se serializan.
final class PanelTablero extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel titulo = Tema.titulo("");
    private final Rejilla rejilla = new Rejilla();
    private final Map<Integer, TarjetaPersonaje> tarjetas = new LinkedHashMap<>();
    private List<Personaje> personajes = List.of();
    private Set<Integer> candidatos = Set.of();
    private boolean interactivo;
    private Integer seleccionado;
    private final IntConsumer alSeleccionar;

    PanelTablero(String nombre, IntConsumer alSeleccionar) {
        super(new BorderLayout(0, 8));
        this.alSeleccionar = alSeleccionar;
        setBackground(Tema.FONDO);
        titulo.setText(nombre);
        add(titulo, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rejilla,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Tema.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(26);
        add(scroll, BorderLayout.CENTER);
        setMinimumSize(new Dimension(150, 160));
    }

    void actualizar(String nombre, List<Personaje> nuevos, Set<Integer> disponibles, boolean habilitado) {
        if (!personajes.equals(nuevos)) {
            cancelarEfectos();
            tarjetas.clear();
            rejilla.removeAll();
            personajes = List.copyOf(nuevos);
            seleccionado = null;
            for (Personaje personaje : personajes) {
                TarjetaPersonaje tarjeta = new TarjetaPersonaje(personaje, () -> elegir(personaje.getId()));
                tarjetas.put(personaje.getId(), tarjeta);
                rejilla.add(tarjeta);
            }
            rejilla.ajustarColumnas();
            rejilla.revalidate();
        }
        candidatos = Set.copyOf(disponibles);
        interactivo = habilitado;
        if (seleccionado != null && !candidatos.contains(seleccionado)) seleccionado = null;
        titulo.setText(nombre + " · " + candidatos.size() + " candidatos");
        refrescarTarjetas();
    }

    Integer seleccion() { return seleccionado; }

    void reiniciarSeleccion() {
        seleccionado = null;
        refrescarTarjetas();
    }
    Personaje personajeSeleccionado() {
        return seleccionado == null ? null : tarjetas.get(seleccionado).personaje();
    }

    private void elegir(int id) {
        if (!interactivo || !candidatos.contains(id)) return;
        seleccionado = id;
        refrescarTarjetas();
        alSeleccionar.accept(id);
    }

    private void refrescarTarjetas() {
        for (TarjetaPersonaje tarjeta : tarjetas.values()) {
            tarjeta.actualizar(!candidatos.contains(tarjeta.id()), interactivo,
                    seleccionado != null && tarjeta.id() == seleccionado);
        }
    }

    void cancelarEfectos() { tarjetas.values().forEach(TarjetaPersonaje::cancelarEfectos); }

    private static final class Rejilla extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        private int columnas = 5;

        Rejilla() {
            super(null);
            setBackground(Tema.FONDO);
            Tema.margen(this, 3);
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent evento) { ajustarColumnas(); }
            });
        }

        void ajustarColumnas() {
            int nuevas = Math.max(1, Math.min(5, (Math.max(getWidth(), 140) - 6 + 8) / 138));
            if (nuevas != columnas) {
                columnas = nuevas;
                revalidate();
            }
        }

        @Override public void doLayout() {
            ajustarColumnas();
            int ancho = Math.min(130, Math.max(1, getWidth() - 6));
            int inicio = Math.max(3, (getWidth() - (columnas * (ancho + 8) - 8)) / 2);
            for (int i = 0; i < getComponentCount(); i++) {
                getComponent(i).setBounds(inicio + (i % columnas) * (ancho + 8),
                        3 + (i / columnas) * 162, ancho, 154);
            }
        }

        @Override public Dimension getPreferredSize() {
            int filas = (getComponentCount() + columnas - 1) / columnas;
            return new Dimension(columnas * 138 - 2, Math.max(0, filas * 162 - 2));
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(688, 420); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) { return 26; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return orientacion == SwingConstants.VERTICAL ? Math.max(26, visible.height - 26) : visible.width;
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
