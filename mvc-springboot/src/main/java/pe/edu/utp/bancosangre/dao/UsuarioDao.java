package pe.edu.utp.bancosangre.dao;

import java.util.Map;

/** DAO de la entidad: Cuentas de usuario del sistema (RF-01, RF-26). */
public interface UsuarioDao extends Dao {
    /** Anota la fecha y hora del último acceso de la cuenta (DNI o correo) que inicia sesión. */
    void registrarAcceso(String usuario);

    /**
     * Inicio de sesión: comprueba usuario (correo o DNI) y contraseña contra la base.
     * Devuelve un registro con nombre, correo y rol, o null si las credenciales no son válidas.
     */
    Map<String, Object> autenticar(String usuario, String clave);
}
