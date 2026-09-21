package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegistrarTemperaturaRequest(
        @NotNull BigDecimal temperatura
) {
}
