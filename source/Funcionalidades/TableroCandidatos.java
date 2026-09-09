package Funcionalidades;

import datos.MazoPersonajes;

import datos.Personaje;
import datos.Pregunta;
import Interfaces.ITableroCandidatos;

public class TableroCandidatos implements ITableroCandidatos {

    private final MazoPersonajes mazo;
    private final boolean[] vivo;
    private int cantidadViva;

    public TableroCandidatos(MazoPersonajes mazo) {
        this.mazo = mazo;
        // Se indexa por ID original, no por posición: los ID ausentes quedan en false.
        this.vivo = new boolean[mazo.getMaxId() + 1];
        for (Personaje p : mazo) {
            vivo[p.getId()] = true;
        }
        this.cantidadViva = mazo.getCantidad();
    }

    // Comparte el mazo, pero copia los descartes para que cada tablero evolucione por separado.
    public TableroCandidatos(TableroCandidatos original) {
        this.mazo = original.mazo;
        this.vivo = original.vivo.clone();
        this.cantidadViva = original.cantidadViva;
    }

    @Override
    public boolean estaVivo(int id) {
        return id >= 1 && id < vivo.length && vivo[id];
    }

    @Override
    public int getCantidadViva() {
        return cantidadViva;
    }

    @Override
    public int descartarSegun(Pregunta pregunta, boolean respuesta) {
        int descartados = 0;
        for (Personaje p : mazo) {
            if (vivo[p.getId()] && pregunta.cumple(p) != respuesta) {
                vivo[p.getId()] = false;
                cantidadViva--;
                descartados++;
            }
        }
        return descartados;
    }

    @Override
    public void descartar(int id) {
        if (estaVivo(id)) {
            vivo[id] = false;
            cantidadViva--;
        }
    }

    @Override
    public Personaje unicoSobreviviente() {
        if (cantidadViva != 1) {
            return null;
        }
        for (Personaje p : mazo) {
            if (vivo[p.getId()]) {
                return p;
            }
        }
        return null;
    }

    @Override
    public int contarSiCumplen(Pregunta pregunta) {
        int cuenta = 0;
        for (Personaje p : mazo) {
            if (vivo[p.getId()] && pregunta.cumple(p)) {
                cuenta++;
            }
        }
        return cuenta;
    }
}
