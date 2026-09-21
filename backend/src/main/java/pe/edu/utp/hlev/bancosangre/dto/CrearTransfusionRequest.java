package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// RF-12/RF-13: la prueba cruzada y el despacho ya se validaron en pasos previos;
// aquí sólo se registra la administración efectiva de una unidad ya despachada.
public record CrearTransfusionRequest(
        @NotNull Long solicitudId,
        @NotBlank String codigoProductoIsbt,
        Boolean reaccionAdversa,
        String detallesReaccion
) {
}
