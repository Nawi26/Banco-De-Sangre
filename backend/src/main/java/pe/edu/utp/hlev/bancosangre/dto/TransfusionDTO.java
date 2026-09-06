package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record TransfusionDTO(
        Long id,
        String codigoSolicitud,
        String pacienteNombreCompleto,
        String codigoProductoIsbt,
        String grupoAbo,
        String factorRh,
        String tecnologoNombreCompleto,
        String medicoNombreCompleto,
        String resultadoPruebaCruzada,
        LocalDateTime fechaTransfusion,
        Boolean reaccionAdversa,
        String detallesReaccion
) {
    public static TransfusionDTO from(pe.edu.utp.hlev.bancosangre.model.Transfusion t) {
        return new TransfusionDTO(
                t.getId(),
                t.getSolicitud().getCodigoSolicitud(),
                t.getPaciente().getNombres() + " " + t.getPaciente().getApellidos(),
                t.getHemocomponente().getCodigoProductoIsbt(),
                t.getHemocomponente().getGrupoAbo(),
                t.getHemocomponente().getFactorRh(),
                t.getTecnologo() != null ? t.getTecnologo().getNombres() + " " + t.getTecnologo().getApellidos() : null,
                t.getMedico() != null ? t.getMedico().getNombres() + " " + t.getMedico().getApellidos() : null,
                t.getResultadoPruebaCruzada(),
                t.getFechaTransfusion(),
                t.getReaccionAdversa(),
                t.getDetallesReaccion()
        );
    }
}
