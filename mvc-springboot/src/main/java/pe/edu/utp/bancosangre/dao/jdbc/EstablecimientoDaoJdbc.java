package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.EstablecimientoDao;

import java.util.List;

/** Implementación JDBC (PostgreSQL) de EstablecimientoDao. */
@Repository
public class EstablecimientoDaoJdbc extends JdbcDaoBase implements EstablecimientoDao {

    public EstablecimientoDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "establecimientos";
    }

    @Override
    public List<Object> listar() {
        return leer("establecimientos");
    }
}
