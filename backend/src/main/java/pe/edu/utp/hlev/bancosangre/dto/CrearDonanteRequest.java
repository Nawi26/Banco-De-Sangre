package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CrearDonanteRequest(
        @NotBlank String tipoDoc,
        @NotBlank String numDoc,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        LocalDate fechaNacimiento,
        String sexo,
        @NotBlank String grupoAbo,
        @NotBlank String factorRh,
        String telefono,
        String direccion
) {
}
