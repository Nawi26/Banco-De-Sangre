package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record ResultadoMarcadorInput(
        @NotBlank String marcador,
        @NotBlank String resultado
) {
}
