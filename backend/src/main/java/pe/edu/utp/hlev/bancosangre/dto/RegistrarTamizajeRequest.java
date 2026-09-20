package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * RF-07: registra una digitación (primera o segunda) de los 7 marcadores serológicos.
 * RF-31: `claveConfirmacion` reautentica al responsable antes de aceptar el registro.
 */
public record RegistrarTamizajeRequest(
        @NotEmpty @Size(min = 7, max = 7) List<ResultadoMarcadorInput> resultados,
        String origen,
        @NotBlank String claveConfirmacion
) {
}
