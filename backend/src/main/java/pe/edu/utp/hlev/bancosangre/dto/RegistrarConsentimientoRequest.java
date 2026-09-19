package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarConsentimientoRequest(
        @NotBlank String tipoValidacion,
        @NotBlank String evidenciaValidacion,
        boolean aceptado
) {
}
