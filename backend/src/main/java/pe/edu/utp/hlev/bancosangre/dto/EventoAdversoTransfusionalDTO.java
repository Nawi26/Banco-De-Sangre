package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record EventoAdversoTransfusionalDTO(
        Long id,
        Long transfusionId,
        String pacienteNombreCompleto,
        String codigoProductoIsbt,
        String tipoReaccion,
        Boolean esInmediata,
        String gravedad,
        String descripcion,
        String accionesTomadas,
        String usuarioNombreCompleto,
        LocalDateTime fechaDeteccion
) {
    public static EventoAdversoTransfusionalDTO from(pe.edu.utp.hlev.bancosangre.model.EventoAdversoTransfusional e) {
        return new EventoAdversoTransfusionalDTO(
                e.getId(),
                e.getTransfusion().getId(),
                e.getTransfusion().getPaciente() != null
                        ? e.getTransfusion().getPaciente().getNombres() + " " + e.getTransfusion().getPaciente().getApellidos()
                        : null,
                e.getTransfusion().getHemocomponente() != null ? e.getTransfusion().getHemocomponente().getCodigoProductoIsbt() : null,
                e.getTipoReaccion(),
                e.getEsInmediata(),
                e.getGravedad(),
                e.getDescripcion(),
                e.getAccionesTomadas(),
                e.getUsuario() != null ? e.getUsuario().getNombres() + " " + e.getUsuario().getApellidos() : null,
                e.getFechaDeteccion()
        );
    }
}
