package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.IntercambioDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de IntercambioDao. */
@Repository
public class IntercambioDaoJdbc extends JdbcDaoBase implements IntercambioDao {

    public IntercambioDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "intercambios";
    }

    @Override
    public List<Object> listar() {
        return leer("intercambios");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("intercambios", registros, correoActor, rolActor);
    }
}
