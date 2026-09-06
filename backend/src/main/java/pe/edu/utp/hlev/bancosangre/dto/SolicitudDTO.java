package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record SolicitudDTO(
        Long id,
        String codigoSolicitud,
        Long pacienteId,
        String pacienteNombreCompleto,
        String medicoNombreCompleto,
        String tipoHemocomponente,
        Integer unidadesSolicitadas,
        String prioridad,
        String estado,
        String indicacionClinica,
        LocalDateTime fechaSolicitud
) {
    public static SolicitudDTO from(pe.edu.utp.hlev.bancosangre.model.Solicitud s) {
        return new SolicitudDTO(
                s.getId(),
                s.getCodigoSolicitud(),
                s.getPaciente().getId(),
                s.getPaciente().getNombres() + " " + s.getPaciente().getApellidos(),
                s.getMedico().getNombres() + " " + s.getMedico().getApellidos(),
                s.getTipoHemocomponente(),
                s.getUnidadesSolicitadas(),
                s.getPrioridad(),
                s.getEstado(),
                s.getIndicacionClinica(),
                s.getFechaSolicitud()
        );
    }
}
