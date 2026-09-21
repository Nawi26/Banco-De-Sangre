package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarEventoAdversoTransfusionalRequest(
        @NotBlank String tipoReaccion,
        @NotNull Boolean esInmediata,
        @NotBlank String gravedad,
        @NotBlank String descripcion,
        String accionesTomadas
) {
}
