package Funcionalidades;

import Interfaces.IArbitroTurno;
import datos.DiagnosticoTurno;
import datos.DiagnosticoTurno.Comparacion;
import datos.DiagnosticoTurno.Motivo;
import datos.MazoPersonajes;
import datos.Personaje;
import datos.Pregunta;
import datos.ResultadoTurno;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

enum EstrategiaMaquina {
    AGRESIVA(5.0, false),
    EQUILIBRADA(2.5, true);

    private final double incrementoPorDescarte;
    private final boolean buscaEquilibrio;

    EstrategiaMaquina(double incrementoPorDescarte, boolean buscaEquilibrio) {
        this.incrementoPorDescarte = incrementoPorDescarte;
        this.buscaEquilibrio = buscaEquilibrio;
    }

    double porcentajeRiesgo(int descartados) {
        return Math.min(100.0, 4.0 + incrementoPorDescarte * descartados);
    }

    Pregunta seleccionarPregunta(TableroCandidatos tablero, Random random) {
        return seleccionarPregunta(tablero, random, new ArrayList<>(), new ArrayList<>());
    }

    // Las comparaciones se conservan antes de filtrar: explican la decisión sin
    // consultar el secreto ni repetir el sorteo cuando la vista muestra el turno.
    private Pregunta seleccionarPregunta(TableroCandidatos tablero, Random random,
            List<Comparacion> comparaciones, List<Pregunta> mejores) {
        int mejorDiferencia = buscaEquilibrio ? Integer.MAX_VALUE : -1;
        int cantidad = tablero.getCantidadViva();
        for (Pregunta pregunta : Pregunta.values()) {
            int cantidadSi = tablero.contarSiCumplen(pregunta);
            Comparacion comparacion = new Comparacion(pregunta, cantidadSi, cantidad - cantidadSi);
            comparaciones.add(comparacion);
            if (comparacion.esConstante()) { continue; }
            int diferencia = comparacion.getDiferencia();
            if (buscaEquilibrio ? diferencia < mejorDiferencia : diferencia > mejorDiferencia) {
                mejores.clear();
                mejorDiferencia = diferencia;
            }
            if (diferencia == mejorDiferencia) { mejores.add(pregunta); }
        }
        return mejores.isEmpty() ? null : mejores.get(random.nextInt(mejores.size()));
    }

    ResultadoTurno ejecutarTurno(String nombre, MazoPersonajes mazo, TableroCandidatos tablero,
            Random random, IArbitroTurno arbitro) {
        Objects.requireNonNull(arbitro);
        int cantidad = tablero.getCantidadViva();
        if (cantidad == 0) {
            throw new IllegalStateException("La máquina no tiene candidatos para continuar");
        }

        // El mazo original es la referencia: incluye los descartes heredados en el riesgo.
        int descartados = mazo.getCantidad() - cantidad;
        double porcentaje = porcentajeRiesgo(descartados);
        Double sorteo = cantidad == 1 ? null : random.nextDouble();
        List<Comparacion> comparaciones = new ArrayList<>();
        List<Pregunta> mejores = new ArrayList<>();
        Motivo motivo = cantidad == 1 ? Motivo.CANDIDATO_UNICO : Motivo.SORTEO_RIESGO;
        if (cantidad > 1 && sorteo >= porcentaje / 100.0) {
            Pregunta pregunta = seleccionarPregunta(tablero, random, comparaciones, mejores);
            if (pregunta != null) {
                DiagnosticoTurno diagnostico = new DiagnosticoTurno(cantidad, descartados,
                        incrementoPorDescarte, porcentaje, sorteo, buscaEquilibrio,
                        Motivo.PREGUNTA, comparaciones, mejores);
                boolean respuesta = arbitro.responder(pregunta);
                int eliminados = tablero.descartarSegun(pregunta, respuesta);
                // Preguntar termina el turno aunque deje un único candidato.
                return ResultadoTurno.pregunta(nombre, pregunta, respuesta, eliminados,
                        tablero.getCantidadViva(), diagnostico);
            }
            motivo = Motivo.SIN_PREGUNTAS;
        }

        DiagnosticoTurno diagnostico = new DiagnosticoTurno(cantidad, descartados,
                incrementoPorDescarte, porcentaje, sorteo, buscaEquilibrio,
                motivo, comparaciones, mejores);
        Personaje candidato;
        if (cantidad == 1) {
            candidato = tablero.unicoSobreviviente();
        } else {
            List<Personaje> vivos = new ArrayList<>();
            for (Personaje personaje : mazo) {
                if (tablero.estaVivo(personaje.getId())) { vivos.add(personaje); }
            }
            candidato = vivos.get(random.nextInt(vivos.size()));
        }
        boolean acierto = arbitro.comprobarIntento(candidato.getId());
        if (!acierto) { tablero.descartar(candidato.getId()); }
        return ResultadoTurno.intento(nombre, candidato, acierto,
                tablero.getCantidadViva(), diagnostico);
    }
}
