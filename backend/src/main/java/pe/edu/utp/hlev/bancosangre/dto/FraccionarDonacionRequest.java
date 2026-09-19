package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// RF-06: tipos de hemocomponente a producir a partir de la bolsa de sangre total.
// Valores admitidos: CONCENTRADO_HEMATIES, PLASMA_FRESCO_CONGELADO, CRIOPRECIPITADO, CONCENTRADO_PLAQUETAS.
public record FraccionarDonacionRequest(
        @NotEmpty List<String> tiposHemocomponente
) {
}
