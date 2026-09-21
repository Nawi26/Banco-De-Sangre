package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearCamaraRequest(
        @NotBlank String nombre,
        @NotBlank String tipo,
        String ubicacion,
        @NotNull BigDecimal temperaturaMinima,
        @NotNull BigDecimal temperaturaMaxima
) {
}
