package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEstadoIntercambioRequest(
        @NotBlank String estado,
        Double temperaturaCadenaFrio
) {
}
