package adivinanzas.dominio;

import static adivinanzas.dominio.ColorPelo.AMARILLO;
import static adivinanzas.dominio.ColorPelo.COLORADO;
import static adivinanzas.dominio.ColorPelo.NEGRO;
import static adivinanzas.dominio.Genero.FEMENINO;
import static adivinanzas.dominio.Genero.MASCULINO;

public final class CatalogoPersonajes {

    private CatalogoPersonajes() {
    }

    public static MazoPersonajes crearMazo() {
        MazoPersonajes mazo = new MazoPersonajes();

        //                nombre     genero      calvo  lentes  pelo
        mazo.agregar("Fito",  MASCULINO, false, false, NEGRO);
        mazo.agregar("Ana",   FEMENINO,  false, false, COLORADO);
        mazo.agregar("Mati",  MASCULINO, true,  true,  AMARILLO);
        mazo.agregar("Hebe",  FEMENINO,  true,  true,  NEGRO);
        mazo.agregar("Caio",  MASCULINO, false, true,  COLORADO);
        mazo.agregar("Irma",  FEMENINO,  false, false, AMARILLO);
        mazo.agregar("Hugo",  MASCULINO, true,  false, NEGRO);
        mazo.agregar("Bea",   FEMENINO,  false, true,  COLORADO);
        mazo.agregar("Joaco", MASCULINO, false, false, AMARILLO);
        mazo.agregar("Gina",  FEMENINO,  true,  false, NEGRO);
        mazo.agregar("Eze",   MASCULINO, true,  true,  COLORADO);
        mazo.agregar("Juana", FEMENINO,  false, true,  AMARILLO);
        mazo.agregar("Ivan",  MASCULINO, true,  true,  NEGRO);
        mazo.agregar("Cora",  FEMENINO,  true,  false, COLORADO);
        mazo.agregar("Lalo",  MASCULINO, true,  false, AMARILLO);
        mazo.agregar("Elsa",  FEMENINO,  false, false, NEGRO);
        mazo.agregar("Beto",  MASCULINO, false, false, COLORADO);
        mazo.agregar("Kari",  FEMENINO,  true,  false, AMARILLO);
        mazo.agregar("Gonza", MASCULINO, false, true,  NEGRO);
        mazo.agregar("Delia", FEMENINO,  true,  true,  COLORADO);
        mazo.agregar("Kevin", MASCULINO, false, true,  AMARILLO);
        mazo.agregar("Flor",  FEMENINO,  false, true,  NEGRO);
        mazo.agregar("Dani",  MASCULINO, true,  false, COLORADO);

        return mazo;
    }
}