package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record ResponderTicketSoporteRequest(
        @NotBlank String estado,
        String respuesta
) {
}
