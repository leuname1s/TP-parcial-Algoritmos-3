import datos.UsuarioArchivo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UsuarioArchivoRegressionTest {
    public static void main(String[] args) throws Exception {
        Path directorio = Files.createTempDirectory("usuario-test-");
        Path archivo = directorio.resolve("usuario.txt");
        try {
            UsuarioArchivo usuario = new UsuarioArchivo(archivo);
            comprobar(usuario.cargar().isEmpty(), "Primera apertura sin usuario");
            usuario.guardar(" bruno ");
            comprobar(new UsuarioArchivo(archivo).cargar().equals("bruno"), "Recuperar al reabrir");
            usuario.guardar("BRUNO");
            comprobar(new UsuarioArchivo(archivo).cargar().equals("BRUNO"), "Conservar mayúsculas elegidas");
            usuario.guardar("José|uno");
            comprobar(new UsuarioArchivo(archivo).cargar().equals("José|uno"), "Conservar UTF-8");
            usuario.guardar("");
            comprobar(new UsuarioArchivo(archivo).cargar().isEmpty(), "Recordar el campo borrado");
            Files.write(archivo, new byte[]{(byte) 0xC3, 0x28});
            try { usuario.cargar(); throw new AssertionError("UTF-8 inválido aceptado"); }
            catch (IOException expected) { }
            comprobar(Files.size(archivo) == 2, "Conservar archivo ilegible");
            try { new UsuarioArchivo(archivo.resolve("hijo")).guardar("Ana");
                throw new AssertionError("Fallo de escritura ocultado"); }
            catch (IOException expected) { }
        } finally {
            Files.deleteIfExists(archivo);
            Files.delete(directorio);
        }
        System.out.println("PASS: último usuario, recarga, cambio, UTF-8, vacío y errores de archivo");
    }

    private static void comprobar(boolean valor, String mensaje) {
        if (!valor) { throw new AssertionError(mensaje); }
    }
}
