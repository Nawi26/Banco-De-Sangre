package pe.edu.utp.bancosangre.dao.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Base común de los DAO con JDBC. Toda la conversión entre tablas y registros vive en las funciones
 * de PostgreSQL (db/funciones.sql); los DAO solo las llaman:
 *   - api_listar(coleccion)                 -> lectura
 *   - api_sincronizar(cambios, correo, rol) -> escritura transaccional
 */
abstract class JdbcDaoBase {

    protected static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<Object>> LISTA = new TypeReference<>() { };

    protected final JdbcTemplate jdbc;

    protected JdbcDaoBase(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    protected List<Object> leer(String coleccion) {
        String texto = jdbc.queryForObject("SELECT api_listar(?)::text", String.class, coleccion);
        try {
            return texto == null ? List.of() : JSON.readValue(texto, LISTA);
        } catch (Exception e) {
            throw new IllegalStateException("Respuesta inválida de la base para " + coleccion, e);
        }
    }

    /** Guarda una lista de registros bajo la clave que entiende api_sincronizar (usuarios, donantes, inventory...). */
    protected void escribir(String clave, List<Map<String, Object>> registros, String correo, String rol) {
        try {
            String cambios = JSON.writeValueAsString(Map.of(clave, registros));
            jdbc.queryForObject("SELECT api_sincronizar(?::jsonb, ?, ?)::text", String.class, cambios, correo, rol);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("No se pudo convertir los registros a JSON", e);
        }
    }
}
