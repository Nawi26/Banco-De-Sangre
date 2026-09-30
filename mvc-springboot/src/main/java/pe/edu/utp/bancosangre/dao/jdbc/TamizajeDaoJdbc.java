package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.TamizajeDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de TamizajeDao. */
@Repository
public class TamizajeDaoJdbc extends JdbcDaoBase implements TamizajeDao {

    public TamizajeDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "serologia";
    }

    @Override
    public List<Object> listar() {
        return leer("serologia");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("serologia", registros, correoActor, rolActor);
    }
}
