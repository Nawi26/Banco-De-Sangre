package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotNull;

public record CrearDonacionRequest(
        @NotNull Long donanteId,
        Integer volumenMl,
        String tipoDonacion,
        Long campanaId
) {
}
