package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearSolicitudRequest(
        @NotNull Long pacienteId,
        @NotBlank String tipoHemocomponente,
        @NotNull @Min(1) Integer unidadesSolicitadas,
        @NotBlank String prioridad,
        String indicacionClinica
) {
}
