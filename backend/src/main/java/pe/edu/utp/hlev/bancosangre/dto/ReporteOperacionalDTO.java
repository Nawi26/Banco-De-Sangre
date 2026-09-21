package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;
import java.util.Map;

// RF-16/RF-36: reporte operacional/estadístico consolidado para fiscalización PRONAHEBAS/DIGDOT/MINSA.
public record ReporteOperacionalDTO(
        LocalDateTime desde,
        LocalDateTime hasta,
        long donacionesRegistradas,
        long transfusionesRealizadas,
        Map<String, Long> descartesPorMotivo,
        long eventosAdversosTransfusionales,
        long eventosAdversosDonacion,
        Map<String, Long> solicitudesPorEstado,
        long certificadosEmitidos
) {
}
