package pe.edu.utp.hlev.bancosangre.dto;

// RF-44: indicador de cumplimiento normativo PRONAHEBAS.
public record IndicadorCumplimientoDTO(
        String codigo,
        String nombre,
        double valorPorcentual,
        String descripcion
) {
}
