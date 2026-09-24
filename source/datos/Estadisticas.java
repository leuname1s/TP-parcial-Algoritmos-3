package datos;

/** Instantánea de contadores. En el marcador global solo se usan los de máquinas. */
public final class Estadisticas {
    private final long partidasTotales, victorias, victoriasVerdaderas, victoriasMaquina1, victoriasMaquina2;

    public Estadisticas(long totales, long victorias, long verdaderas, long maquina1, long maquina2) {
        this.partidasTotales = totales;
        this.victorias = victorias;
        this.victoriasVerdaderas = verdaderas;
        this.victoriasMaquina1 = maquina1;
        this.victoriasMaquina2 = maquina2;
    }
    public long getPartidasTotales() { return partidasTotales; }
    public long getVictorias() { return victorias; }
    public long getVictoriasVerdaderas() { return victoriasVerdaderas; }
    public long getVictoriasMaquina1() { return victoriasMaquina1; }
    public long getVictoriasMaquina2() { return victoriasMaquina2; }
}
