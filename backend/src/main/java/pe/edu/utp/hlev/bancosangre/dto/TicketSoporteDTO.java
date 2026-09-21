package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record TicketSoporteDTO(
        Long id,
        String usuarioNombreCompleto,
        String asunto,
        String descripcion,
        String prioridad,
        String estado,
        String respuesta,
        String resueltoPorNombreCompleto,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {
    public static TicketSoporteDTO from(pe.edu.utp.hlev.bancosangre.model.TicketSoporte t) {
        return new TicketSoporteDTO(
                t.getId(),
                t.getUsuario().getNombres() + " " + t.getUsuario().getApellidos(),
                t.getAsunto(),
                t.getDescripcion(),
                t.getPrioridad(),
                t.getEstado(),
                t.getRespuesta(),
                t.getResueltoPor() != null ? t.getResueltoPor().getNombres() + " " + t.getResueltoPor().getApellidos() : null,
                t.getCreadoEn(),
                t.getActualizadoEn()
        );
    }
}
