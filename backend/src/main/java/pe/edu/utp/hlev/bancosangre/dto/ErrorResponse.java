package pe.edu.utp.hlev.bancosangre.dto;

public record ErrorResponse(
        boolean error,
        String mensaje
) {
    public static ErrorResponse of(String mensaje) {
        return new ErrorResponse(true, mensaje);
    }
}
