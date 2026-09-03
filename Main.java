package adivinanzas;

import adivinanzas.dominio.CatalogoPersonajes;
import adivinanzas.dominio.MazoPersonajes;
import adivinanzas.dominio.Personaje;
import adivinanzas.dominio.Pregunta;
import adivinanzas.dominio.TableroCandidatos;

public class Main {

    public static void main(String[] args) {
        MazoPersonajes mazo = CatalogoPersonajes.crearMazo();
        Personaje objetivo = mazo.buscarPorId(2);   // Cora
        TableroCandidatos tablero = new TableroCandidatos(mazo);

        System.out.println("Objetivo secreto: " + objetivo);
        System.out.println("Candidatos vivos: " + tablero.getCantidadViva());

        System.out.println();
        System.out.println("== Cuanto parte cada pregunta al inicio ==");
        for (Pregunta q : Pregunta.values()) {
            int cumplen = tablero.contarSiCumplen(q);
            System.out.printf("   %-28s si=%2d  no=%2d%n",
                    q, cumplen, tablero.getCantidadViva() - cumplen);
        }

        System.out.println();
        System.out.println("== Interrogatorio ==");
        Pregunta[] interrogatorio = {
                Pregunta.ES_FEMENINO,
                Pregunta.PELO_COLORADO,
                Pregunta.ES_CALVO,
                Pregunta.USA_LENTES
        };
        for (Pregunta q : interrogatorio) {
            boolean respuesta = q.cumple(objetivo);
            int bajados = tablero.descartarSegun(q, respuesta);
            System.out.printf("   %-28s -> %-3s  descarto %2d, quedan %2d%n",
                    q, respuesta ? "SI" : "NO", bajados, tablero.getCantidadViva());
        }

        System.out.println();
        Personaje unico = tablero.unicoSobreviviente();
        System.out.println(unico != null
                ? "Deduccion: " + unico.getNombre()
                : "Todavia quedan " + tablero.getCantidadViva() + " candidatos");
    }
}