package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CrearConvenioRequest(
        @NotNull Long ipressId,
        @NotBlank String numeroConvenio,
        @NotBlank String objeto,
        @NotNull LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {
}
