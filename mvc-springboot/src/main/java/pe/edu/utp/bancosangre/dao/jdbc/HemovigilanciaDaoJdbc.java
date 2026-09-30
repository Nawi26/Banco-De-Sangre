package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.HemovigilanciaDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de HemovigilanciaDao. */
@Repository
public class HemovigilanciaDaoJdbc extends JdbcDaoBase implements HemovigilanciaDao {

    public HemovigilanciaDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "hemovigilancia";
    }

    @Override
    public List<Object> listar() {
        return leer("hemovigilancia");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("hemovigilancia", registros, correoActor, rolActor);
    }
}
