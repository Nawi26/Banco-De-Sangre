package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record PublicarProtocoloRequest(
        @NotBlank String nombre,
        @NotBlank String version,
        String contenidoUrl,
        String notasCambio
) {
}
