package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record ProtocoloClinicoDTO(
        Long id,
        String nombre,
        String version,
        String contenidoUrl,
        String notasCambio,
        Boolean vigente,
        String publicadoPorNombreCompleto,
        LocalDateTime fechaPublicacion
) {
    public static ProtocoloClinicoDTO from(pe.edu.utp.hlev.bancosangre.model.ProtocoloClinico p) {
        return new ProtocoloClinicoDTO(
                p.getId(), p.getNombre(), p.getVersion(), p.getContenidoUrl(), p.getNotasCambio(), p.getVigente(),
                p.getPublicadoPor() != null ? p.getPublicadoPor().getNombres() + " " + p.getPublicadoPor().getApellidos() : null,
                p.getFechaPublicacion()
        );
    }
}
