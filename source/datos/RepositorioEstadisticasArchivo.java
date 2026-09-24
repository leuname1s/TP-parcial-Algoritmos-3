package datos;

import Interfaces.IRepositorioEstadisticas;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;

public final class RepositorioEstadisticasArchivo implements IRepositorioEstadisticas {
    private final Path archivo;

    public RepositorioEstadisticasArchivo() { this(Path.of("data", "estadisticas.properties")); }
    public RepositorioEstadisticasArchivo(Path archivo) {
        this.archivo = Objects.requireNonNull(archivo).toAbsolutePath();
    }

    @Override public Properties cargar() throws IOException {
        Properties registro = new Properties();
        if (Files.notExists(archivo)) { return registro; }
        try (Reader reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            registro.load(reader);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Archivo de estadísticas inválido", ex);
        }
        if (registro.isEmpty()) { throw new IOException("Archivo de estadísticas vacío; no se sobrescribe"); }
        return registro;
    }

    @Override public void guardar(Properties registro) throws IOException {
        Files.createDirectories(archivo.getParent());
        Path temporal = Files.createTempFile(archivo.getParent(), "estadisticas-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                registro.store(writer, "Resultados definitivos - version 1");
            }
            // Si no hay reemplazo atómico, se informa el fallo y se conserva el archivo anterior.
            Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
