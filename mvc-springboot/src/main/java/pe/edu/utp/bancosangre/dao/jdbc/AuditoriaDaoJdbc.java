package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.AuditoriaDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de AuditoriaDao. */
@Repository
public class AuditoriaDaoJdbc extends JdbcDaoBase implements AuditoriaDao {

    public AuditoriaDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "auditoria";
    }

    @Override
    public List<Object> listar() {
        return leer("auditoria");
    }

    @Override
    public void registrar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("auditNuevos", registros, correoActor, rolActor);
    }
}
