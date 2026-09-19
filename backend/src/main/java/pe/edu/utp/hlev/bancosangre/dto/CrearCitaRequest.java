package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CrearCitaRequest(
        @NotNull Long donanteId,
        Long campanaId,
        @NotNull LocalDateTime fechaHora,
        String notas
) {
}
