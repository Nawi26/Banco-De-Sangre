package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record EventoDisponibilidadDTO(
        String tipo,
        LocalDateTime fecha
) {
    public static EventoDisponibilidadDTO from(pe.edu.utp.hlev.bancosangre.model.EventoDisponibilidad e) {
        return new EventoDisponibilidadDTO(e.getTipo(), e.getFecha());
    }
}
