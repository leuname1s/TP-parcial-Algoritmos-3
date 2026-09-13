package datos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Conserva el último nombre local, separado del historial de partidas. */
public final class UsuarioArchivo {
    private final Path archivo;

    public UsuarioArchivo(Path archivo) {
        this.archivo = Objects.requireNonNull(archivo).toAbsolutePath();
    }

    public String cargar() throws IOException {
        return Files.notExists(archivo) ? "" : Files.readString(archivo, StandardCharsets.UTF_8).strip();
    }

    public void guardar(String usuario) throws IOException {
        String nombre = Objects.requireNonNull(usuario).strip();
        Files.createDirectories(archivo.getParent());
        Path temporal = Files.createTempFile(archivo.getParent(), "usuario-", ".tmp");
        try {
            Files.writeString(temporal, nombre, StandardCharsets.UTF_8);
            Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
