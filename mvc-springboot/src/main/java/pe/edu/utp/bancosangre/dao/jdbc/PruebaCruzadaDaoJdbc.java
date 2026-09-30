package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.PruebaCruzadaDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de PruebaCruzadaDao. */
@Repository
public class PruebaCruzadaDaoJdbc extends JdbcDaoBase implements PruebaCruzadaDao {

    public PruebaCruzadaDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "pruebas";
    }

    @Override
    public List<Object> listar() {
        return leer("pruebas");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("pruebas", registros, correoActor, rolActor);
    }
}
