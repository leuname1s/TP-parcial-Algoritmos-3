package datos;

import Interfaces.EvaluadorPregunta;

public enum Pregunta implements EvaluadorPregunta {

    ES_FEMENINO("Es mujer") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getGenero() == Genero.FEMENINO;
        }
    },
    ES_MASCULINO("Es hombre") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getGenero() == Genero.MASCULINO;
        }
    },
    ES_CALVO("Es pelado") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.PELADO;
        }
    },
    USA_LENTES("Usa lentes") {
        @Override
        public boolean cumple(Personaje p) {
            return p.isTieneLentes();
        }
    },
    PELO_PELIRROJO("Tiene el pelo pelirrojo") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.PELIRROJO;
        }
    },
    PELO_NEGRO("Tiene el pelo negro") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.NEGRO;
        }
    },
    PELO_RUBIO("Tiene el pelo rubio") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.RUBIO;
        }
    },
    TIENE_BARBA("Tiene barba") {
        @Override
        public boolean cumple(Personaje p) {
            return p.isTieneBarba();
        }
    },
    LE_FALTA_UN_DIENTE("Le falta un diente") {
        @Override
        public boolean cumple(Personaje p) {
            return p.isLeFaltaUnDiente();
        }
    };

    private final String texto;

    Pregunta(String texto) {
        this.texto = texto;
    }

    @Override
    public abstract boolean cumple(Personaje p);

    public String getTexto() {
        return texto;
    }

    @Override
    public String toString() {
        return texto + "?";
    }
}
