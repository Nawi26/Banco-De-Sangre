package pe.edu.utp.bancosangre.dao.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.UsuarioDao;

import java.util.List;
import java.util.Map;

/** Implementación JDBC (PostgreSQL) de UsuarioDao. */
@Repository
public class UsuarioDaoJdbc extends JdbcDaoBase implements UsuarioDao {

    public UsuarioDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "usuarios";
    }

    @Override
    public List<Object> listar() {
        return leer("usuarios");
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("usuarios", registros, correoActor, rolActor);
    }

    @Override
    public void registrarAcceso(String usuario) {
        jdbc.queryForObject("SELECT api_registrar_acceso(?)::text", String.class, usuario);
    }

    @Override
    public Map<String, Object> autenticar(String usuario, String clave) {
        String texto = jdbc.queryForObject("SELECT api_autenticar(?, ?)::text", String.class, usuario, clave);
        if (texto == null) {
            return null;
        }
        try {
            return JSON.readValue(texto, new TypeReference<Map<String, Object>>() { });
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta inválida de la base al iniciar sesión", e);
        }
    }
}
