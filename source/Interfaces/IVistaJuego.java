package Interfaces;

import Controladores.EstadoVistaJuego;

/** Contrato de presentación para conectar una vista al controlador. */
public interface IVistaJuego {
    void actualizar(EstadoVistaJuego estado);
    void mostrarError(String mensaje);
    boolean confirmarAbandono();
    boolean confirmarSalidaSinGuardar();
    void cerrar();
}
