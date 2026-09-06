package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearIntercambioRequest(
        @NotNull Long ipressSolicitanteId,
        @NotNull Long ipressProveedoraId,
        @NotBlank String codigoProductoIsbt,
        String responsableTransporte
) {
}
