package Interfaces;

import Controladores.EstadoVistaJuego;

/** Contrato de presentación para conectar Swing o una vista de prueba. */
public interface IVistaJuego {
    void actualizar(EstadoVistaJuego estado);
    void mostrarError(String mensaje);
    boolean confirmarAbandono();
    boolean confirmarSalidaSinGuardar();
    void cerrar();
}
