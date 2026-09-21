package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record EventoAdversoDonacionDTO(
        Long id,
        Long donacionId,
        String donanteNombreCompleto,
        String tipoEvento,
        String gravedad,
        String descripcion,
        String accionesTomadas,
        String usuarioNombreCompleto,
        LocalDateTime fechaDeteccion
) {
    public static EventoAdversoDonacionDTO from(pe.edu.utp.hlev.bancosangre.model.EventoAdversoDonacion e) {
        return new EventoAdversoDonacionDTO(
                e.getId(),
                e.getDonacion().getId(),
                e.getDonacion().getDonante() != null
                        ? e.getDonacion().getDonante().getNombres() + " " + e.getDonacion().getDonante().getApellidos()
                        : null,
                e.getTipoEvento(),
                e.getGravedad(),
                e.getDescripcion(),
                e.getAccionesTomadas(),
                e.getUsuario() != null ? e.getUsuario().getNombres() + " " + e.getUsuario().getApellidos() : null,
                e.getFechaDeteccion()
        );
    }
}
