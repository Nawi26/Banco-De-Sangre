package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

// RF-41: respuesta pública (sin autenticación) al escanear el QR de un certificado.
public record VerificacionCertificadoDTO(
        boolean valido,
        String din,
        String codigoProductoIsbt,
        String tipoHemocomponente,
        String grupoAboRh,
        LocalDateTime fechaVencimiento,
        String estadoActual,
        LocalDateTime emitidoEn
) {
    public static VerificacionCertificadoDTO invalido() {
        return new VerificacionCertificadoDTO(false, null, null, null, null, null, null, null);
    }
}
