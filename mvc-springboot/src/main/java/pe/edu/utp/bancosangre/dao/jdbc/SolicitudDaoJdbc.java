package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.SolicitudDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de SolicitudDao. */
@Repository
public class SolicitudDaoJdbc extends JdbcDaoBase implements SolicitudDao {

    public SolicitudDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "solicitudes";
    }

    @Override
    public List<Object> listar() {
        return leer("solicitudes");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("solicitudes", registros, correoActor, rolActor);
    }
}
