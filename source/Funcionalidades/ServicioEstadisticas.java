package Funcionalidades;

import Interfaces.IRepositorioEstadisticas;
import datos.Estadisticas;
import datos.ModoJuego;
import datos.ResultadoPartida;
import datos.ResultadoPartida.Desenlace;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

/** Registro local de una sola instancia; no coordina procesos concurrentes. */
public final class ServicioEstadisticas {
    private final IRepositorioEstadisticas repositorio;

    public ServicioEstadisticas(IRepositorioEstadisticas repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public boolean registrar(String usuario, ResultadoPartida resultado) throws IOException {
        Objects.requireNonNull(resultado, "La partida debe haber terminado");
        String nombre = resultado.getModo() == ModoJuego.MAQUINA_1_VS_MAQUINA_2 ? "" : normalizar(usuario);
        Properties registro = cargarValidado();
        String clave = resultado.getId().toString();
        String valor = resultado.getModo().name() + "|" + resultado.getDesenlace().name() + "|" + nombre;
        if (registro.containsKey(clave)) {
            if (!valor.equals(registro.getProperty(clave))) {
                throw new IllegalArgumentException("La partida ya está registrada con otros datos");
            }
            return false;
        }
        registro.setProperty("version", "1");
        registro.setProperty(clave, valor);
        repositorio.guardar(registro);
        return true;
    }

    public Estadisticas consultarUsuario(String usuario) throws IOException { return consultar(normalizar(usuario)); }
    public Estadisticas consultarMaquinas() throws IOException { return consultar(null); }

    private Estadisticas consultar(String usuario) throws IOException {
        Properties registro = cargarValidado();
        long total = 0, victorias = 0, verdaderas = 0, maquina1 = 0, maquina2 = 0;
        for (String clave : registro.stringPropertyNames()) {
            if (clave.equals("version")) { continue; }
            String[] datos = registro.getProperty(clave).split("\\|", 3);
            boolean maquinas = datos[0].equals(ModoJuego.MAQUINA_1_VS_MAQUINA_2.name());
            if (usuario == null ? !maquinas : maquinas || !usuario.equals(datos[2])) { continue; }
            total++;
            switch (Desenlace.valueOf(datos[1])) {
                case VICTORIA_VERDADERA: verdaderas++; victorias++; break;
                case VICTORIA: victorias++; break;
                case GANA_MAQUINA_1: maquina1++; break;
                case GANA_MAQUINA_2: maquina2++; break;
                default: break;
            }
        }
        return new Estadisticas(total, victorias, verdaderas, maquina1, maquina2);
    }

    private Properties cargarValidado() throws IOException {
        Properties registro = repositorio.cargar();
        if (registro.isEmpty()) { return registro; }
        try {
            if (!"1".equals(registro.getProperty("version"))) { throw new IllegalArgumentException(); }
            for (String clave : registro.stringPropertyNames()) {
                if (clave.equals("version")) { continue; }
                UUID id = UUID.fromString(clave);
                if (!id.toString().equals(clave)) { throw new IllegalArgumentException(); }
                String[] datos = registro.getProperty(clave).split("\\|", 3);
                if (datos.length != 3) { throw new IllegalArgumentException(); }
                ModoJuego modo = ModoJuego.valueOf(datos[0]);
                new ResultadoPartida(id, modo, Desenlace.valueOf(datos[1]));
                if (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2) {
                    if (!datos[2].isEmpty()) { throw new IllegalArgumentException(); }
                } else if (!normalizar(datos[2]).equals(datos[2])) { throw new IllegalArgumentException(); }
            }
        } catch (IllegalArgumentException ex) {
            throw new IOException("Registro de estadísticas inválido; se conserva el archivo", ex);
        }
        return registro;
    }

    private static String normalizar(String usuario) {
        String nombre = Objects.requireNonNull(usuario, "Falta el usuario").strip();
        if (nombre.isEmpty()) { throw new IllegalArgumentException("El nombre no puede estar vacío"); }
        return nombre;
    }
}
