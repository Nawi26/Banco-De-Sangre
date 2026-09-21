package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearSolicitudRequest(
        @NotNull Long pacienteId,
        @NotBlank String tipoHemocomponente,
        @NotNull @Min(1) Integer unidadesSolicitadas,
        @NotBlank String prioridad,
        String indicacionClinica,
        // RF-11: diagnóstico que sustenta la solicitud, codificado en CIE-10 (ej. "D50", "O99.0").
        @NotBlank String diagnosticoCie10
) {
}
