package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record IntercambioDTO(
        Long id,
        String estado,
        Double temperaturaCadenaFrio,
        String responsableTransporte,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaRespuesta,
        String codigoProductoIsbt,
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        String hospitalSolicitante,
        String hospitalProveedor
) {
}
