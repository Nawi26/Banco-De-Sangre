package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarUbicacionRequest(
        @NotNull Long camaraId
) {
}
