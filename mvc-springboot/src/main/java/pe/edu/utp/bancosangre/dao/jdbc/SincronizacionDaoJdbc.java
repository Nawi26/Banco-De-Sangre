package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.SincronizacionDao;

/** Implementación JDBC (PostgreSQL) de SincronizacionDao. */
@Repository
public class SincronizacionDaoJdbc extends JdbcDaoBase implements SincronizacionDao {

    public SincronizacionDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public void sincronizar(String cambiosJson, String correoActor, String rolActor) {
        jdbc.queryForObject("SELECT api_sincronizar(?::jsonb, ?, ?)::text", String.class, cambiosJson, correoActor, rolActor);
    }

    @Override
    public void vaciar() {
        jdbc.queryForObject("SELECT api_vaciar()::text", String.class);
    }
}
