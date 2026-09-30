package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.DonanteDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de DonanteDao. */
@Repository
public class DonanteDaoJdbc extends JdbcDaoBase implements DonanteDao {

    public DonanteDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "donantes";
    }

    @Override
    public List<Object> listar() {
        return leer("donantes");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("donantes", registros, correoActor, rolActor);
    }
}
