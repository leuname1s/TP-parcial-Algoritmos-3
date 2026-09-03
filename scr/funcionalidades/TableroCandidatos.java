package funcionalidades;

import datos.Personaje;
import datos.Pregunta;

public class TableroCandidatos {

    private final MazoPersonajes mazo;
    private final boolean[] vivo;
    private int cantidadViva;

    public TableroCandidatos(MazoPersonajes mazo) {
        this.mazo = mazo;
        this.vivo = new boolean[MazoPersonajes.TOTAL + 1];
        for (int id = 1; id <= mazo.getCantidad(); id++) {
            vivo[id] = true;
        }
        this.cantidadViva = mazo.getCantidad();
    }

    public boolean estaVivo(int id) {
        return id >= 1 && id <= MazoPersonajes.TOTAL && vivo[id];
    }

    public int getCantidadViva() {
        return cantidadViva;
    }

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

    public void descartar(int id) {
        if (estaVivo(id)) {
            vivo[id] = false;
            cantidadViva--;
        }
    }

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
