package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DisponibilidadDTO(
        LocalDateTime enLineaDesde,
        long segundosActivo,
        List<EventoDisponibilidadDTO> historial
) {
}
