package pe.edu.utp.bancosangre.dao;

import java.util.List;
import java.util.Map;

/** DAO de la entidad: Alertas del sistema. Solo se agregan alertas nuevas (no se editan). */
public interface AlertaDao extends LecturaDao {
    /** Agrega alertas nuevas. */
    void registrar(List<Map<String, Object>> alertas, String correoActor, String rolActor);
}
