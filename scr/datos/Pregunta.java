package datos;

import interfaces.EvaluadorPregunta;

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
            return p.isTienePelo();
        }
    },
    USA_LENTES("Usa lentes") {
        @Override
        public boolean cumple(Personaje p) {
            return p.isTieneLentes();
        }
    },
    PELO_COLORADO("Tiene el pelo colorado") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.COLORADO;
        }
    },
    PELO_NEGRO("Tiene el pelo negro") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.NEGRO;
        }
    },
    PELO_AMARILLO("Tiene el pelo amarillo") {
        @Override
        public boolean cumple(Personaje p) {
            return p.getColorPelo() == ColorPelo.AMARILLO;
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
