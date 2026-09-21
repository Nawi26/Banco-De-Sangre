package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record MensajeDTO(
        Long id,
        Long solicitudId,
        String remitenteNombreCompleto,
        String remitenteRol,
        String contenido,
        LocalDateTime fechaEnvio
) {
    public static MensajeDTO from(pe.edu.utp.hlev.bancosangre.model.Mensaje m) {
        return new MensajeDTO(
                m.getId(),
                m.getSolicitud().getId(),
                m.getRemitente().getNombres() + " " + m.getRemitente().getApellidos(),
                m.getRemitente().getRol().getNombre(),
                m.getContenido(),
                m.getFechaEnvio()
        );
    }
}
