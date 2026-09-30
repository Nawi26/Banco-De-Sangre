package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.AlertaDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de AlertaDao. */
@Repository
public class AlertaDaoJdbc extends JdbcDaoBase implements AlertaDao {

    public AlertaDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "alertas";
    }

    @Override
    public List<Object> listar() {
        return leer("alertas");
    }

    @Override
    public void registrar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("alertasNuevas", registros, correoActor, rolActor);
    }
}
