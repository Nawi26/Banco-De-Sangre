package pe.edu.utp.hlev.bancosangre.dto;

public record UsuarioAdminDTO(
        Long id,
        String dni,
        String nombres,
        String apellidos,
        String email,
        String colegiatura,
        Boolean estado,
        String rolNombre
) {
    public static UsuarioAdminDTO from(pe.edu.utp.hlev.bancosangre.model.Usuario u) {
        return new UsuarioAdminDTO(
                u.getId(),
                u.getDni(),
                u.getNombres(),
                u.getApellidos(),
                u.getEmail(),
                u.getColegiatura(),
                u.getEstado(),
                u.getRol().getNombre()
        );
    }
}
