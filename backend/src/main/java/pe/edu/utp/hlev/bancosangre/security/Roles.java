package pe.edu.utp.hlev.bancosangre.security;

/**
 * Nombres canónicos de los 4 roles de negocio (RF-02).
 * Deben coincidir exactamente con roles.nombre en base de datos
 * (ver V2__auditoria_y_roles_negocio.sql), ya que el JWT propaga este valor
 * como autoridad de Spring Security.
 */
public final class Roles {

    public static final String ADMINISTRADOR = "Administrador";
    public static final String JEFE_BANCO_SANGRE = "Jefe de Banco de Sangre";
    public static final String TECNOLOGO_MEDICO = "Tecnólogo Médico";
    public static final String MEDICO_SOLICITANTE = "Médico Solicitante";

    private Roles() {
    }
}
