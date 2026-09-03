package adivinanzas.dominio;

public enum Genero {
    FEMENINO(0),
    MASCULINO(1);

    private final int order;

    Genero(int order){
        this.order = order;
    }

    public int getOrden(){
        return order;
    }

}
