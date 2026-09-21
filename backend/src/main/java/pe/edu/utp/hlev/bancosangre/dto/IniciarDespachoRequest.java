package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IniciarDespachoRequest(
        @NotNull Long solicitudId,
        @NotBlank String codigoProductoIsbt,
        @NotBlank String claveConfirmacion
) {
}
