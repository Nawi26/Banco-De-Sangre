package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RegistrarMantenimientoRequest(
        @NotBlank String nombreEquipo,
        @NotBlank String tipoEquipo,
        @NotBlank String tipoMantenimiento,
        @NotNull LocalDateTime fechaRealizado,
        LocalDateTime fechaProximoVencimiento,
        String observaciones
) {
}
