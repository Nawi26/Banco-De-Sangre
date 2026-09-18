package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record AuditoriaLogDTO(
        Long id,
        Long usuarioId,
        String usuarioIdentificador,
        String rol,
        String metodoHttp,
        String endpoint,
        String accion,
        String ipOrigen,
        Integer resultadoHttp,
        Boolean exitoso,
        String detalle,
        LocalDateTime creadoEn
) {
}
