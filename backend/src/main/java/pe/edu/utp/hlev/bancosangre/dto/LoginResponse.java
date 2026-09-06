package pe.edu.utp.hlev.bancosangre.dto;

public record LoginResponse(
        boolean exito,
        String token,
        UsuarioSesionDTO usuario
) {
}
