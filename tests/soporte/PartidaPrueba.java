package soporte;

import Funcionalidades.Partida;
import datos.MazoPersonajes;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import datos.ResultadoTurno;
import defaults.CatalogoPersonajes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Escenarios reproducibles por la API pública; nunca modifican campos privados del motor. */
public final class PartidaPrueba {
    public final Partida partida;
    public final List<Personaje> personajes = new ArrayList<>();
    public final Personaje primerSecreto;
    public final Personaje segundoSecreto;
    public final Personaje humano;

    public PartidaPrueba(ModoJuego modo, boolean humanoUltimo) {
        this(modo, humanoUltimo, new AzarFijo(0.0, 0), new AzarFijo(0.0, 0));
    }

    public PartidaPrueba(ModoJuego modo, boolean humanoUltimo, Random maquina1, Random maquina2) {
        Random esperado = new Random(9);
        MazoPersonajes mazo = CatalogoPersonajes.crearMazo(esperado);
        for (Personaje personaje : mazo) { personajes.add(personaje); }
        primerSecreto = personajes.get(esperado.nextInt(personajes.size()));
        List<Personaje> siguientes = new ArrayList<>(personajes);
        siguientes.remove(primerSecreto);
        segundoSecreto = siguientes.get(esperado.nextInt(siguientes.size()));
        humano = personajes.get(humanoUltimo ? personajes.size() - 1 : 0);
        partida = new Partida(new Random(9), maquina1, maquina2);
        partida.iniciar(modo);
        partida.seleccionarPersonaje(humano.getId());
    }

    public ResultadoTurno fallarJugador() {
        Personaje secreto = partida.getFase() == 1 ? primerSecreto : segundoSecreto;
        for (Personaje candidato : partida.getCandidatos(Participante.JUGADOR)) {
            if (!candidato.equals(secreto)) { return partida.arriesgar(candidato.getId()); }
        }
        throw new AssertionError("No quedan intentos incorrectos disponibles");
    }

    public void avanzarFallos(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            comprobar(!fallarJugador().isAcierto(), "El intento debía fallar");
            comprobar(!partida.ejecutarTurnoMaquina().isAcierto(), "La máquina debía fallar");
        }
    }

    public static final class AzarFijo extends Random {
        private static final long serialVersionUID = 1L;
        private final double sorteo;
        private final int indice;

        public AzarFijo(double sorteo, int indice) {
            super(0);
            this.sorteo = sorteo;
            this.indice = indice;
        }

        @Override public double nextDouble() { return sorteo; }
        @Override public int nextInt(int limite) {
            comprobar(indice >= 0 && indice < limite, "Índice de prueba fuera de rango");
            return indice;
        }
    }

    public static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) { throw new AssertionError(mensaje); }
    }

    public static void rechaza(Class<? extends RuntimeException> tipo, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException ex) {
            if (tipo.isInstance(ex)) { return; }
            throw new AssertionError("Excepción inesperada", ex);
        }
        throw new AssertionError("La acción debía rechazarse con " + tipo.getSimpleName());
    }
}
