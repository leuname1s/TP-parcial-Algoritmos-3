package Interfaces;

import java.io.IOException;
import java.util.Properties;

/** Cada carga devuelve una copia independiente del registro persistido. */
public interface IRepositorioEstadisticas {
    Properties cargar() throws IOException;
    void guardar(Properties registro) throws IOException;
}
