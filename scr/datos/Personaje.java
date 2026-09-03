package datos;

import interfaces.IPersonaje;
import java.util.Objects;

public class Personaje implements IPersonaje {
    private final int id;
    private final String nombre;
    private final Genero genero;
    private final ColorPelo colorPelo;
    private final boolean tieneLentes;
    private final boolean tienePelo;

    public Personaje(int id, String nombre, Genero genero, ColorPelo colorPelo, boolean tieneLentes, boolean tienePelo) {
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre);
        this.genero = Objects.requireNonNull(genero);
        this.colorPelo = Objects.requireNonNull(colorPelo);
        this.tieneLentes = tieneLentes;
        this.tienePelo = tienePelo;
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
    public boolean isTienePelo() { return tienePelo; }

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
        return String.format("[%02d] %-10s %s | %s | %s | pelo %s",
                id, nombre,
                genero == Genero.FEMENINO ? "F" : "M",
                tienePelo ? "calvo " : "peludo",
                tieneLentes ? "lentes  " : "sin lentes",
                colorPelo);
    }
}
