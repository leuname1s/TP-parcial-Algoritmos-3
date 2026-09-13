package GUI;

import Controladores.TurnoRegistrado;
import datos.DiagnosticoTurno;
import datos.ResultadoPartida;
import datos.ResultadoTurno;
import java.util.Locale;
import java.util.stream.Collectors;

final class TextoPartida {
    private TextoPartida() { }

    static String resumen(TurnoRegistrado turno) {
        ResultadoTurno r = turno.resultado();
        String accion = r.getPregunta() != null
                ? "¿" + r.getPregunta().getTexto() + "? " + (r.getRespuesta() ? "Sí." : "No.")
                : "Arriesgó por " + r.getIntento().getNombre() + " (ID " + r.getIntento().getId() + "). "
                        + (r.isAcierto() ? "¡Acertó!" : "No acertó.");
        String descarte = r.getPregunta() == null ? "" : "\nSe descartan los que "
                + (r.getRespuesta() ? "no cumplen" : "cumplen") + " esa característica.";
        return "Fase " + turno.fase() + " · Ronda " + turno.ronda() + " · " + r.getParticipante()
                + "\n" + accion + "\n" + r.getDescartados() + " descartados · "
                + r.getCandidatosRestantes() + " candidatos restantes." + descarte;
    }

    static String razonamiento(DiagnosticoTurno d) {
        if (d == null) return "";
        StringBuilder texto = new StringBuilder();
        texto.append("Antes de actuar: ").append(d.getCandidatosAntes()).append(" candidatos y ")
                .append(d.getDescartadosAntes()).append(" descartados.\n")
                .append("Riesgo = min(100, 4 + ").append(numero(d.getIncremento())).append(" × ")
                .append(d.getDescartadosAntes()).append(") = ").append(numero(d.getPorcentajeRiesgo())).append("%.\n");
        texto.append(d.getSorteo() == null ? "Sin sorteo: quedó un único candidato.\n"
                : "Sorteo: " + numero(d.getSorteo() * 100) + "% "
                        + (d.getSorteo() < d.getPorcentajeRiesgo() / 100 ? "<" : "≥")
                        + " riesgo " + numero(d.getPorcentajeRiesgo()) + "%.\n");
        texto.append("Decisión: ").append(switch (d.getMotivo()) {
            case PREGUNTA -> "preguntar para reducir candidatos";
            case SORTEO_RIESGO -> "arriesgar por el resultado del sorteo";
            case CANDIDATO_UNICO -> "arriesgar por el único candidato";
            case SIN_PREGUNTAS -> "arriesgar porque no quedan preguntas útiles";
        }).append(".\n");
        texto.append(d.isBuscaEquilibrio() ? "Estrategia: minimizar la diferencia entre sí y no.\n"
                : "Estrategia: maximizar la diferencia entre sí y no.\n");
        if (!d.getComparaciones().isEmpty()) {
            texto.append("Evaluación de preguntas disponibles:\n");
            for (DiagnosticoTurno.Comparacion c : d.getComparaciones()) {
                texto.append("• ").append(c.getPregunta().getTexto()).append(": sí ")
                        .append(c.getCantidadSi()).append(" / no ").append(c.getCantidadNo())
                        .append(" · diferencia ").append(c.getDiferencia())
                        .append(c.esConstante() ? " · no reduce candidatos"
                                : " · descartaría " + c.getCantidadNo() + " con sí / "
                                        + c.getCantidadSi() + " con no").append("\n");
            }
        }
        if (!d.getMejoresPreguntas().isEmpty()) {
            texto.append("Mejores preguntas según su estrategia: ").append(d.getMejoresPreguntas().stream()
                    .map(p -> p.getTexto()).collect(Collectors.joining("; "))).append(".\n");
            texto.append(d.getMejoresPreguntas().size() > 1 ? "El empate se resuelve al azar entre esas preguntas.\n"
                    : "Hay una sola pregunta con la diferencia elegida.\n");
        }
        if (d.getMotivo() != DiagnosticoTurno.Motivo.PREGUNTA && d.getCandidatosAntes() > 1) {
            texto.append("El personaje intentado se elige al azar: cada candidato tiene probabilidad 1/")
                    .append(d.getCandidatosAntes()).append(".\n");
        }
        return texto.toString().stripTrailing();
    }

    static String resultado(ResultadoPartida resultado) {
        return switch (resultado.getDesenlace()) {
            case DERROTA -> "Esta vez ganó la máquina";
            case VICTORIA -> "¡Ganaste la partida!";
            case VICTORIA_VERDADERA -> "¡Victoria verdadera!";
            case GANA_MAQUINA_1 -> "Ganó la Máquina 1";
            case GANA_MAQUINA_2 -> "Ganó la Máquina 2";
        };
    }

    private static String numero(double valor) { return String.format(Locale.forLanguageTag("es-AR"), "%.2f", valor); }
}
