package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.DespachoDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de DespachoDao. */
@Repository
public class DespachoDaoJdbc extends JdbcDaoBase implements DespachoDao {

    public DespachoDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("despachosNuevos", registros, correoActor, rolActor);
    }
}
