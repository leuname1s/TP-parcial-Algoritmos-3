package GUI;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Recursos visuales por ID; el motor no depende de imágenes ni de sus rutas. */
final class ImagenesPersonajes {
    private static final Map<Integer, BufferedImage> ORIGINALES = new HashMap<>();
    private static final Map<String, ImageIcon> TAMANOS = new HashMap<>();

    private ImagenesPersonajes() { }

    static ImageIcon obtener(int id, int lado) {
        String clave = id + ":" + lado;
        return TAMANOS.computeIfAbsent(clave, ignorada -> escalar(original(id), lado));
    }

    private static BufferedImage original(int id) {
        if (ORIGINALES.containsKey(id)) return ORIGINALES.get(id);
        BufferedImage imagen = null;
        String nombre = String.format(java.util.Locale.ROOT, "%02d.png", id);
        Path ruta = Path.of("resources", "personajes", nombre);
        try {
            if (Files.isRegularFile(ruta)) imagen = ImageIO.read(ruta.toFile());
            if (imagen == null) {
                try (InputStream entrada = ImagenesPersonajes.class.getResourceAsStream("/personajes/" + nombre)) {
                    if (entrada != null) imagen = ImageIO.read(entrada);
                }
            }
        } catch (IOException | SecurityException excepcion) {
            // La ausencia de un recurso visual no impide jugar.
        }
        ORIGINALES.put(id, imagen);
        return imagen;
    }

    private static ImageIcon escalar(BufferedImage imagen, int lado) {
        if (imagen == null) return new ImageIcon();
        BufferedImage copia = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D grafico = copia.createGraphics();
        grafico.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double escala = Math.min((double) lado / imagen.getWidth(), (double) lado / imagen.getHeight());
        int ancho = (int) Math.round(imagen.getWidth() * escala);
        int alto = (int) Math.round(imagen.getHeight() * escala);
        grafico.drawImage(imagen, (lado - ancho) / 2, (lado - alto) / 2, ancho, alto, null);
        grafico.dispose();
        return new ImageIcon(copia);
    }
}
