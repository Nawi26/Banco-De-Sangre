package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarEventoAdversoDonacionRequest(
        @NotBlank String tipoEvento,
        @NotBlank String gravedad,
        @NotBlank String descripcion,
        String accionesTomadas
) {
}
