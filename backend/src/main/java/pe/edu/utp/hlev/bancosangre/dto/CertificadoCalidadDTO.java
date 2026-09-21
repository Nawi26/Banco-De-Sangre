package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record CertificadoCalidadDTO(
        Long hemocomponenteId,
        String codigoProductoIsbt,
        String numeroCertificado,
        String codigoVerificacion,
        LocalDateTime emitidoEn
) {
}
