package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearTicketSoporteRequest(
        @NotBlank String asunto,
        @NotBlank String descripcion,
        @NotBlank String prioridad
) {
}
