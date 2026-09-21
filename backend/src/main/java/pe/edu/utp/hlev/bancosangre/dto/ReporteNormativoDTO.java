package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record ReporteNormativoDTO(
        Long id,
        LocalDateTime periodoDesde,
        LocalDateTime periodoHasta,
        LocalDateTime fechaGeneracion,
        Long donacionesRegistradas,
        Long transfusionesRealizadas,
        Long descartesTotal,
        Long eventosAdversosTotal,
        Long certificadosEmitidos
) {
    public static ReporteNormativoDTO from(pe.edu.utp.hlev.bancosangre.model.ReporteNormativoGenerado r) {
        return new ReporteNormativoDTO(
                r.getId(), r.getPeriodoDesde(), r.getPeriodoHasta(), r.getFechaGeneracion(),
                r.getDonacionesRegistradas(), r.getTransfusionesRealizadas(), r.getDescartesTotal(),
                r.getEventosAdversosTotal(), r.getCertificadosEmitidos()
        );
    }
}
