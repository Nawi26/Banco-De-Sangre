package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.HemocomponenteEvento;

import java.time.LocalDateTime;

public record HemocomponenteEventoDTO(
        String tipoEvento,
        String detalle,
        String usuarioNombreCompleto,
        LocalDateTime fecha
) {
    public static HemocomponenteEventoDTO from(HemocomponenteEvento e) {
        return new HemocomponenteEventoDTO(
                e.getTipoEvento(),
                e.getDetalle(),
                e.getUsuario() != null ? e.getUsuario().getNombres() + " " + e.getUsuario().getApellidos() : null,
                e.getFecha()
        );
    }
}
