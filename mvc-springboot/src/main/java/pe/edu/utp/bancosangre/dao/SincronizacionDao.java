package pe.edu.utp.bancosangre.dao;

/**
 * Guarda de una sola vez todos los cambios que envía la pantalla (donantes, inventario, solicitudes...),
 * en UNA transacción. Es la "unidad de trabajo": si una parte falla, no se guarda nada.
 */
public interface SincronizacionDao {

    void sincronizar(String cambiosJson, String correoActor, String rolActor);

    /** Vacía todas las tablas (solo para "Restablecer datos de ejemplo"). */
    void vaciar();
}
