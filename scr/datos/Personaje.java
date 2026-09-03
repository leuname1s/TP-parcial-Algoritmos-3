package datos;

import java.util.Objects;

public class Personaje {
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

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public Genero getGenero() { return genero; }
    public ColorPelo getColorPelo() { return colorPelo; }
    public boolean isTieneLentes() { return tieneLentes; }
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
