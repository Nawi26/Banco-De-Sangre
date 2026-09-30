package pe.edu.utp.bancosangre.dao;

import java.util.List;
import java.util.Map;

/**
 * Patrón DAO — contrato de ESCRITURA: inserta o actualiza registros de la entidad.
 * Nada se elimina: el historial clínico no se borra, se cambia de estado.
 */
public interface EscrituraDao {

    /** Guarda (inserta o actualiza) los registros en UNA transacción: si algo falla, no se guarda nada. */
    void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor);
}
