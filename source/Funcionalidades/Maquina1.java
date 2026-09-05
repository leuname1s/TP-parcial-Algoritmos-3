package Funcionalidades;

import datos.MazoPersonajes;

import datos.Personaje;
import Interfaces.IArbitroTurno;
import Interfaces.IMaquina;
import Interfaces.ITableroCandidatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maquina1 implements IMaquina {

    private final String nombre = "Máquina 1";
    private final TableroCandidatos tablero;
    private final MazoPersonajes mazo;
    private final Random random;

    public Maquina1(MazoPersonajes mazo) {
        this.mazo = mazo;
        this.tablero = new TableroCandidatos(mazo);
        this.random = new Random();
    }

    public Maquina1(MazoPersonajes mazo, TableroCandidatos tableroHeredado) {
        this.mazo = mazo;
        this.tablero = new TableroCandidatos(tableroHeredado);
        this.random = new Random();
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
    public boolean ejecutarTurno(IArbitroTurno arbitro) {
        System.out.println("\n--- TURNO DE " + nombre.toUpperCase() + " ---");

        List<Personaje> vivos = new ArrayList<>();
        for (Personaje p : mazo) {
            if (tablero.estaVivo(p.getId())) {
                vivos.add(p);
            }
        }

        if (vivos.isEmpty()) {
            System.out.println("[" + nombre + "] No le quedan candidatos vivos.");
            return false;
        }

        Personaje candidatoElegido = vivos.get(random.nextInt(vivos.size()));
        System.out.println("[" + nombre + "] Intenta adivinar arriesgando por: "
                + candidatoElegido.getNombre() + " (ID: " + candidatoElegido.getId() + ")");

        if (arbitro.comprobarIntento(candidatoElegido.getId())) {
            System.out.println("\n**************************************************");
            System.out.println("   ¡" + nombre.toUpperCase() + " HA ADIVINADO EL PERSONAJE!   ");
            System.out.println("   El personaje era: " + candidatoElegido.getNombre() + " (ID: " + candidatoElegido.getId() + ")");
            System.out.println("**************************************************\n");
            return true;
        } else {
            System.out.println("[" + nombre + "] Falló la adivinanza.");
            tablero.descartar(candidatoElegido.getId());
            System.out.println("[" + nombre + "] Descartó a " + candidatoElegido.getNombre()
                    + " de su tablero. Le quedan " + tablero.getCantidadViva() + " candidatos.");
            return false;
        }
    }
}
