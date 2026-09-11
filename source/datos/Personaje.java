package datos;

import Interfaces.IPersonaje;
import java.util.Objects;

public class Personaje implements IPersonaje {
    private final int id;
    private final String nombre;
    private final Genero genero;
    private final ColorPelo colorPelo;
    private final boolean tieneLentes;
    private final boolean tieneBarba;
    private final boolean leFaltaUnDiente;

    public Personaje(int id, String nombre, Genero genero, ColorPelo colorPelo, boolean tieneLentes, boolean tieneBarba, boolean leFaltaUnDiente) {
        if (id < 1) {
            throw new IllegalArgumentException("El ID debe ser positivo");
        }
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre);
        this.genero = Objects.requireNonNull(genero);
        this.colorPelo = Objects.requireNonNull(colorPelo);
        this.tieneLentes = tieneLentes;
        this.tieneBarba = tieneBarba;
        this.leFaltaUnDiente = leFaltaUnDiente;
    }

    @Override
    public int getId() { return id; }

    @Override
    public String getNombre() { return nombre; }

    @Override
    public Genero getGenero() { return genero; }

    @Override
    public ColorPelo getColorPelo() { return colorPelo; }

    @Override
    public boolean isTieneLentes() { return tieneLentes; }

    @Override
    public boolean isTieneBarba() { return tieneBarba; }

    @Override
    public boolean isLeFaltaUnDiente() { return leFaltaUnDiente; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Personaje)) return false;
        return id == ((Personaje) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("[%02d] %-16s %s | %s | %s | %s | %s",
                id, nombre,
                genero == Genero.FEMENINO ? "F" : "M",
                colorPelo == ColorPelo.PELADO ? "pelado" : "pelo " + colorPelo,
                tieneLentes ? "con lentes" : "sin lentes",
                tieneBarba ? "con barba" : "sin barba",
                leFaltaUnDiente ? "le falta un diente" : "dentadura completa");
    }
}
