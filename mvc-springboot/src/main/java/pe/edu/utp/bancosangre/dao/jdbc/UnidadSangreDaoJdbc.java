package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.UnidadSangreDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de UnidadSangreDao. */
@Repository
public class UnidadSangreDaoJdbc extends JdbcDaoBase implements UnidadSangreDao {

    public UnidadSangreDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "inventario";
    }

    @Override
    public List<Object> listar() {
        return leer("inventario");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("inventory", registros, correoActor, rolActor);
    }
}
