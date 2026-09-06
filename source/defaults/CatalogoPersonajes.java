package defaults;

import datos.MazoPersonajes;
import datos.Personaje;
import datos.ColorPelo;
import datos.Genero;
import Interfaces.ICatalogoPersonajes;
import java.util.Random;

import static datos.ColorPelo.NEGRO;
import static datos.ColorPelo.PELADO;
import static datos.ColorPelo.PELIRROJO;
import static datos.ColorPelo.RUBIO;
import static datos.Genero.FEMENINO;
import static datos.Genero.MASCULINO;

public final class CatalogoPersonajes implements ICatalogoPersonajes {
    public static final int TOTAL_CATALOGO = 36;

    @Override
    public Iterable<Personaje> obtenerPersonajes() {
        return crearCatalogo();
    }

    public static MazoPersonajes crearCatalogo() {
        MazoPersonajes catalogo = new MazoPersonajes(TOTAL_CATALOGO);

        agregar(catalogo, "Isabella", FEMENINO, NEGRO, true, true, true);
        agregar(catalogo, "Sofía", FEMENINO, NEGRO, true, true, false);
        agregar(catalogo, "Catalina", FEMENINO, NEGRO, true, false, false);
        agregar(catalogo, "Valentina", FEMENINO, NEGRO, false, true, false);
        agregar(catalogo, "Agustina", FEMENINO, NEGRO, false, false, false);
        agregar(catalogo, "Camila", FEMENINO, RUBIO, true, true, true);
        agregar(catalogo, "Clara", FEMENINO, RUBIO, true, false, true);
        agregar(catalogo, "Martina", FEMENINO, RUBIO, true, false, false);
        agregar(catalogo, "Victoria", FEMENINO, RUBIO, false, true, false);
        agregar(catalogo, "Emilia", FEMENINO, RUBIO, false, false, false);
        agregar(catalogo, "Zoe", FEMENINO, PELIRROJO, true, true, false);
        agregar(catalogo, "Renata", FEMENINO, PELIRROJO, true, false, false);
        agregar(catalogo, "Malena", FEMENINO, PELIRROJO, false, true, false);
        agregar(catalogo, "Julieta", FEMENINO, PELIRROJO, false, false, false);
        agregar(catalogo, "Pilar", FEMENINO, PELADO, true, true, true);
        agregar(catalogo, "Mía", FEMENINO, PELADO, true, false, false);
        agregar(catalogo, "Olivia", FEMENINO, PELADO, false, true, false);
        agregar(catalogo, "Alma", FEMENINO, PELADO, false, false, false);
        agregar(catalogo, "Roc Marciano", MASCULINO, NEGRO, true, true, false);
        agregar(catalogo, "Mateo", MASCULINO, NEGRO, true, false, false);
        agregar(catalogo, "Bruno", MASCULINO, NEGRO, false, true, false);
        agregar(catalogo, "Gael", MASCULINO, NEGRO, false, false, false);
        agregar(catalogo, "Pablito Lescano", MASCULINO, RUBIO, true, true, false);
        agregar(catalogo, "Thiago", MASCULINO, RUBIO, true, false, false);
        agregar(catalogo, "Bautista", MASCULINO, RUBIO, false, true, false);
        agregar(catalogo, "Benjamín", MASCULINO, RUBIO, false, false, false);
        agregar(catalogo, "Lautaro", MASCULINO, PELIRROJO, true, true, false);
        agregar(catalogo, "Nicolás", MASCULINO, PELIRROJO, true, false, false);
        agregar(catalogo, "Simón", MASCULINO, PELIRROJO, false, true, true);
        agregar(catalogo, "Dante", MASCULINO, PELIRROJO, false, true, false);
        agregar(catalogo, "Felipe", MASCULINO, PELIRROJO, false, false, false);
        agregar(catalogo, "Tomás", MASCULINO, PELADO, true, true, false);
        agregar(catalogo, "Joaquín", MASCULINO, PELADO, true, false, false);
        agregar(catalogo, "Franco", MASCULINO, PELADO, false, true, false);
        agregar(catalogo, "Agustín", MASCULINO, PELADO, false, false, true);
        agregar(catalogo, "Santiago", MASCULINO, PELADO, false, false, false);
        return catalogo;
    }

    private static void agregar(MazoPersonajes catalogo, String nombre, Genero genero,
                                ColorPelo pelo, boolean lentes, boolean barba, boolean faltaDiente) {
        catalogo.agregar(new Personaje(catalogo.getCantidad() + 1, nombre, genero,
                pelo, lentes, barba, faltaDiente));
    }

    public static MazoPersonajes crearMazo() {
        return crearMazo(new Random());
    }

    public static MazoPersonajes crearMazo(Random random) {
        MazoPersonajes catalogo = crearCatalogo();
        Personaje[] personajes = new Personaje[catalogo.getCantidad()];
        int posicion = 0;
        for (Personaje p : catalogo) {
            personajes[posicion++] = p;
        }
        // Fisher-Yates samples without replacement while preserving character IDs.
        for (int i = personajes.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Personaje temporal = personajes[i];
            personajes[i] = personajes[j];
            personajes[j] = temporal;
        }
        MazoPersonajes mazo = new MazoPersonajes();
        for (int i = 0; i < MazoPersonajes.TOTAL; i++) {
            mazo.agregar(personajes[i]);
        }
        return mazo;
    }
}
