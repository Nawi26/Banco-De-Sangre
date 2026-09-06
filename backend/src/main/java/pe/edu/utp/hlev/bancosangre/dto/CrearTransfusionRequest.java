package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearTransfusionRequest(
        @NotNull Long solicitudId,
        @NotBlank String codigoProductoIsbt,
        String resultadoPruebaCruzada,
        Boolean reaccionAdversa,
        String detallesReaccion
) {
}
