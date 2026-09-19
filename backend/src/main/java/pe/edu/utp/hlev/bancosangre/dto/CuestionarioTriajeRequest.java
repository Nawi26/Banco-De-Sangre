package pe.edu.utp.hlev.bancosangre.dto;

import java.math.BigDecimal;

// RF-03/RF-04: respuestas del triaje clínico-epidemiológico previo a la donación.
public record CuestionarioTriajeRequest(
        BigDecimal pesoKg,
        BigDecimal tallaCm,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer pulso,
        BigDecimal hemoglobina,
        boolean antecedenteIts,
        boolean antecedenteUsoDrogas,
        boolean tatuajeOPerforacionReciente,
        boolean embarazoOPartoReciente,
        boolean viajeZonaEndemica,
        String otrosAntecedentes
) {
}
