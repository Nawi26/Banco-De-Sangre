package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.StockDao;

import java.util.List;

/** Implementación JDBC (PostgreSQL) de StockDao. */
@Repository
public class StockDaoJdbc extends JdbcDaoBase implements StockDao {

    public StockDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "stock";
    }

    @Override
    public List<Object> listar() {
        return leer("stock");
    }
}
