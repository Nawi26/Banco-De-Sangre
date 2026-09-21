package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record TurnoPersonalDTO(
        Long id,
        Long usuarioId,
        String usuarioNombreCompleto,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String tipoTurno,
        String observaciones
) {
    public static TurnoPersonalDTO from(pe.edu.utp.hlev.bancosangre.model.TurnoPersonal t) {
        return new TurnoPersonalDTO(
                t.getId(), t.getUsuario().getId(), t.getUsuario().getNombres() + " " + t.getUsuario().getApellidos(),
                t.getFechaInicio(), t.getFechaFin(), t.getTipoTurno(), t.getObservaciones()
        );
    }
}
