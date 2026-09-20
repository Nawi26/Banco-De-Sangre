package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ResolverDiscordanciaRequest(
        @NotEmpty List<ResultadoMarcadorInput> resultadosDefinitivos,
        @NotBlank String claveConfirmacion
) {
}
