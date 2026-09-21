package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarPruebaCompatibilidadRequest(
        @NotBlank String codigoProductoIsbt,
        @NotBlank String resultadoRai,
        @NotBlank String resultadoPruebaCruzada
) {
}
