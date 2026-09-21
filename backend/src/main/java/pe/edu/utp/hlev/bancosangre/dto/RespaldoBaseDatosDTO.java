package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record RespaldoBaseDatosDTO(
        Long id,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String rutaArchivo,
        Long tamanioBytes,
        String hashIntegridad,
        String estado,
        String detalle
) {
    public static RespaldoBaseDatosDTO from(pe.edu.utp.hlev.bancosangre.model.RespaldoBaseDatos r) {
        return new RespaldoBaseDatosDTO(
                r.getId(), r.getFechaInicio(), r.getFechaFin(), r.getRutaArchivo(), r.getTamanioBytes(),
                r.getHashIntegridad(), r.getEstado(), r.getDetalle()
        );
    }
}
