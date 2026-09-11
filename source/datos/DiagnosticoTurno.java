package datos;

import java.util.List;
import java.util.Objects;

/** Datos de la decisión ya tomada; la presentación no vuelve a ejecutar la estrategia. */
public final class DiagnosticoTurno {
    public enum Motivo { PREGUNTA, SORTEO_RIESGO, CANDIDATO_UNICO, SIN_PREGUNTAS }

    public static final class Comparacion {
        private final Pregunta pregunta;
        private final int cantidadSi;
        private final int cantidadNo;

        public Comparacion(Pregunta pregunta, int cantidadSi, int cantidadNo) {
            this.pregunta = Objects.requireNonNull(pregunta);
            this.cantidadSi = cantidadSi;
            this.cantidadNo = cantidadNo;
        }

        public Pregunta getPregunta() { return pregunta; }
        public int getCantidadSi() { return cantidadSi; }
        public int getCantidadNo() { return cantidadNo; }
        public int getDiferencia() { return Math.abs(cantidadSi - cantidadNo); }
        public boolean esConstante() { return cantidadSi == 0 || cantidadNo == 0; }
    }

    private final int candidatosAntes;
    private final int descartadosAntes;
    private final double incremento;
    private final double porcentajeRiesgo;
    private final Double sorteo;
    private final boolean buscaEquilibrio;
    private final Motivo motivo;
    private final List<Comparacion> comparaciones;
    private final List<Pregunta> mejoresPreguntas;

    public DiagnosticoTurno(int candidatosAntes, int descartadosAntes, double incremento,
            double porcentajeRiesgo, Double sorteo, boolean buscaEquilibrio, Motivo motivo,
            List<Comparacion> comparaciones, List<Pregunta> mejoresPreguntas) {
        this.candidatosAntes = candidatosAntes;
        this.descartadosAntes = descartadosAntes;
        this.incremento = incremento;
        this.porcentajeRiesgo = porcentajeRiesgo;
        this.sorteo = sorteo;
        this.buscaEquilibrio = buscaEquilibrio;
        this.motivo = Objects.requireNonNull(motivo);
        this.comparaciones = List.copyOf(comparaciones);
        this.mejoresPreguntas = List.copyOf(mejoresPreguntas);
    }

    public int getCandidatosAntes() { return candidatosAntes; }
    public int getDescartadosAntes() { return descartadosAntes; }
    public double getIncremento() { return incremento; }
    public double getPorcentajeRiesgo() { return porcentajeRiesgo; }
    /** Sorteo entre 0 y 1; null si el único candidato obligó a arriesgar sin sortear. */
    public Double getSorteo() { return sorteo; }
    public boolean isBuscaEquilibrio() { return buscaEquilibrio; }
    public Motivo getMotivo() { return motivo; }
    public List<Comparacion> getComparaciones() { return comparaciones; }
    public List<Pregunta> getMejoresPreguntas() { return mejoresPreguntas; }
}
