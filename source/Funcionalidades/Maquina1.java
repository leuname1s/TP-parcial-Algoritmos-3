package Funcionalidades;

import datos.MazoPersonajes;
import datos.ResultadoTurno;
import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import Interfaces.ITableroCandidatos;

import java.util.Objects;
import java.util.Random;

public class Maquina1 implements IMaquina {

    private final String nombre = "Máquina 1";
    private final TableroCandidatos tablero;
    private final MazoPersonajes mazo;
    private final Random random;

    public Maquina1(MazoPersonajes mazo) {
        this(mazo, new TableroCandidatos(mazo), new Random());
    }

    public Maquina1(MazoPersonajes mazo, TableroCandidatos tableroHeredado) {
        this(mazo, tableroHeredado, new Random());
    }

    public Maquina1(MazoPersonajes mazo, TableroCandidatos tableroHeredado, Random random) {
        this.mazo = Objects.requireNonNull(mazo);
        this.tablero = new TableroCandidatos(tableroHeredado);
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public ITableroCandidatos getTablero() {
        return tablero;
    }

    @Override
    public ResultadoTurno ejecutarTurno(IArbitroTurno arbitro) {
        return EstrategiaMaquina.AGRESIVA.ejecutarTurno(nombre, mazo, tablero, random, arbitro);
    }
}
