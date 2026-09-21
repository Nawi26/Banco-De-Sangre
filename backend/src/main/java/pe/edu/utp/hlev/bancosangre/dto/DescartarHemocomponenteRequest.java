package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record DescartarHemocomponenteRequest(
        @NotBlank String motivo,
        String evidenciaFoto,
        @NotBlank String claveConfirmacion
) {
}
