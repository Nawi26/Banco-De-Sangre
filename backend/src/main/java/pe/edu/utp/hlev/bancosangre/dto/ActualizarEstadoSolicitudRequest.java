package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEstadoSolicitudRequest(
        @NotBlank String estado
) {
}
