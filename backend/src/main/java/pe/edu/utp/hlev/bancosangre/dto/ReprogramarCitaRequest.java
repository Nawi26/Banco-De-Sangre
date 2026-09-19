package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ReprogramarCitaRequest(
        LocalDateTime fechaHora,
        @NotBlank String estado,
        String notas
) {
}
