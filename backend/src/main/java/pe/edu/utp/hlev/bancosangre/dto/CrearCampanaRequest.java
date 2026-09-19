package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CrearCampanaRequest(
        @NotBlank String nombre,
        String institucion,
        String tipo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer metaUnidades
) {
}
