import Funcionalidades.ServicioEstadisticas;
import Interfaces.IRepositorioEstadisticas;
import datos.*;
import datos.ResultadoPartida.Desenlace;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class EstadisticasRegressionTest {
    public static void main(String[] args) throws Exception {
        Path directorio = Files.createTempDirectory("estadisticas-test-");
        Path archivo = directorio.resolve("marcador.properties");
        try {
            RepositorioEstadisticasArchivo repo = new RepositorioEstadisticasArchivo(archivo);
            ServicioEstadisticas servicio = new ServicioEstadisticas(repo);
            check(servicio.consultarUsuario("Ana").getPartidasTotales() == 0, "Archivo ausente");
            ResultadoPartida victoria = resultado(Desenlace.VICTORIA);
            check(servicio.registrar("  José|uno  ", victoria), "Primer registro");
            check(!servicio.registrar("José|uno", victoria), "Duplicado en sesión");
            servicio = new ServicioEstadisticas(new RepositorioEstadisticasArchivo(archivo));
            check(!servicio.registrar("José|uno", victoria), "Duplicado tras recarga");
            servicio.registrar("José|uno", resultado(Desenlace.VICTORIA_VERDADERA));
            servicio.registrar("José|uno", resultado(Desenlace.DERROTA));
            servicio.registrar("josé|uno", resultado(Desenlace.DERROTA));
            servicio.registrar(null, resultado(Desenlace.GANA_MAQUINA_1));
            servicio.registrar(null, resultado(Desenlace.GANA_MAQUINA_2));
            Estadisticas humano = servicio.consultarUsuario("José|uno");
            check(humano.getPartidasTotales() == 3 && humano.getVictorias() == 2
                    && humano.getVictoriasVerdaderas() == 1, "Contadores humanos");
            check(servicio.consultarUsuario("josé|uno").getPartidasTotales() == 1, "Usuarios independientes");
            Estadisticas maquinas = servicio.consultarMaquinas();
            check(maquinas.getPartidasTotales() == 2 && maquinas.getVictoriasMaquina1() == 1
                    && maquinas.getVictoriasMaquina2() == 1, "Contadores globales");
            try { servicio.registrar("Otro", victoria); throw new AssertionError("Conflicto aceptado"); }
            catch (IllegalArgumentException expected) { }
            try { servicio.consultarUsuario("  "); throw new AssertionError("Usuario vacío aceptado"); }
            catch (IllegalArgumentException expected) { }
            Files.writeString(archivo, "version=99\n");
            try { servicio.registrar("Ana", victoria); throw new AssertionError("Archivo inválido aceptado"); }
            catch (IOException expected) { }
            check(Files.readString(archivo).equals("version=99\n"), "Se alteró el archivo inválido");
            IRepositorioEstadisticas fallo = new IRepositorioEstadisticas() {
                private boolean fallar = true;
                private Properties datos = new Properties();
                public Properties cargar() { return (Properties) datos.clone(); }
                public void guardar(Properties registro) throws IOException {
                    if (fallar) { fallar = false; throw new IOException("Fallo simulado"); }
                    datos = (Properties) registro.clone();
                }
            };
            servicio = new ServicioEstadisticas(fallo);
            try { servicio.registrar("Ana", victoria); throw new AssertionError("Fallo ocultado"); }
            catch (IOException expected) { }
            check(servicio.consultarUsuario("Ana").getPartidasTotales() == 0, "Guardado fallido contabilizado");
            check(servicio.registrar("Ana", victoria), "Reintento fallido");
            check(!servicio.registrar("Ana", victoria), "Reintento duplicado");
        } finally {
            Files.deleteIfExists(archivo);
            Files.delete(directorio);
        }
        System.out.println("PASS: estadísticas, UTF-8, recarga, duplicados, datos inválidos y reintento");
    }
    private static ResultadoPartida resultado(Desenlace desenlace) {
        boolean maquinas = desenlace == Desenlace.GANA_MAQUINA_1 || desenlace == Desenlace.GANA_MAQUINA_2;
        return new ResultadoPartida(UUID.randomUUID(), maquinas ? ModoJuego.MAQUINA_1_VS_MAQUINA_2
                : ModoJuego.JUGADOR_VS_MAQUINA_1, desenlace);
    }
    private static void check(boolean valor, String mensaje) { if (!valor) { throw new AssertionError(mensaje); } }
}
