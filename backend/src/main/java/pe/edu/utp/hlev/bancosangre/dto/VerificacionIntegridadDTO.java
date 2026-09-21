package pe.edu.utp.hlev.bancosangre.dto;

public record VerificacionIntegridadDTO(
        Long respaldoId,
        boolean integro,
        String mensaje
) {
}
