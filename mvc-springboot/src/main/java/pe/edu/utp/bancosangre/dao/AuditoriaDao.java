package pe.edu.utp.bancosangre.dao;

import java.util.List;
import java.util.Map;

/** DAO de la entidad: Bitácora de auditoría. Solo se agregan acciones nuevas (nunca se modifican ni se borran). */
public interface AuditoriaDao extends LecturaDao {
    /** Agrega acciones nuevas a la bitácora. */
    void registrar(List<Map<String, Object>> acciones, String correoActor, String rolActor);
}
