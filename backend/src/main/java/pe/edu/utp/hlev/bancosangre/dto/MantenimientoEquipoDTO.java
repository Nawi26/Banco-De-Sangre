package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record MantenimientoEquipoDTO(
        Long id,
        String nombreEquipo,
        String tipoEquipo,
        String tipoMantenimiento,
        LocalDateTime fechaRealizado,
        LocalDateTime fechaProximoVencimiento,
        String responsableNombreCompleto,
        String observaciones
) {
    public static MantenimientoEquipoDTO from(pe.edu.utp.hlev.bancosangre.model.MantenimientoEquipo m) {
        return new MantenimientoEquipoDTO(
                m.getId(), m.getNombreEquipo(), m.getTipoEquipo(), m.getTipoMantenimiento(),
                m.getFechaRealizado(), m.getFechaProximoVencimiento(),
                m.getResponsable() != null ? m.getResponsable().getNombres() + " " + m.getResponsable().getApellidos() : null,
                m.getObservaciones()
        );
    }
}
