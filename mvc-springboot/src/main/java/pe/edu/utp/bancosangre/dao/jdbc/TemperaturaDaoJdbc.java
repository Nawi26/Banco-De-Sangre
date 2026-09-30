package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.TemperaturaDao;

import java.util.List;

/** Implementación JDBC (PostgreSQL) de TemperaturaDao. */
@Repository
public class TemperaturaDaoJdbc extends JdbcDaoBase implements TemperaturaDao {

    public TemperaturaDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "temperaturas";
    }

    @Override
    public List<Object> listar() {
        return leer("temperaturas");
    }
}
