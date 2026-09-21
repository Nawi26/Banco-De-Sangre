package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CrearReservaQuirurgicaRequest(
        @NotNull Long pacienteId,
        @NotBlank String tipoHemocomponente,
        @NotBlank String grupoAbo,
        @NotBlank String factorRh,
        @NotNull @Min(1) Integer unidadesSolicitadas,
        @NotNull @Future LocalDateTime fechaCirugiaProgramada,
        @Min(1) Integer horasValidezPostCirugia
) {
}
