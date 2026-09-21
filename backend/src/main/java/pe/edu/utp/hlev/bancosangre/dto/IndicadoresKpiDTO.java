package pe.edu.utp.hlev.bancosangre.dto;

// RF-26: panel gerencial de indicadores clave (KPIs) en tiempo real.
public record IndicadoresKpiDTO(
        double rotacionInventario,
        double tasaMermaCaducidadPorcentaje,
        Double tiempoRespuestaPromedioMinutos,
        long unidadesTransfundidasEnPeriodo,
        long unidadesFraccionadasEnPeriodo,
        long inventarioActivoActual
) {
}
