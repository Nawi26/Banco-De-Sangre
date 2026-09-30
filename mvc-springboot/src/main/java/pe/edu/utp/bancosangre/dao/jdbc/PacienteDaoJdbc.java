package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.PacienteDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de PacienteDao. */
@Repository
public class PacienteDaoJdbc extends JdbcDaoBase implements PacienteDao {

    public PacienteDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "pacientes";
    }

    @Override
    public List<Object> listar() {
        return leer("pacientes");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("pacientes", registros, correoActor, rolActor);
    }
}
