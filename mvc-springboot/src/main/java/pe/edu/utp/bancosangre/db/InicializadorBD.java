package pe.edu.utp.bancosangre.db;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import pe.edu.utp.bancosangre.service.BancoSangreService;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Solo si app.db.inicializar=true, al arrancar prepara la base: crea las tablas y funciones si faltan y carga los
 * datos iniciales solo si la base esta vacia. Se puede ejecutar cuantas veces se
 * quiera sin duplicar nada (los scripts son idempotentes).
 */
@Component
public class InicializadorBD implements ApplicationRunner {

    private static final String[] SCRIPTS = {"db/esquema.sql", "db/funciones.sql", "db/datos.sql"};

    private final DataSource dataSource;
    private final BancoSangreService servicio;

    /** false (por defecto): la base se prepara a mano con bancosangre_bd.sql y Spring solo se conecta. */
    private final boolean inicializar;

    public InicializadorBD(DataSource dataSource, BancoSangreService servicio,
                           @Value("${app.db.inicializar:false}") boolean inicializar) {
        this.dataSource = dataSource;
        this.servicio = servicio;
        this.inicializar = inicializar;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!inicializar) {
            return;
        }
        for (String s : SCRIPTS) {
            ejecutar(s);
        }
    }

    /** Borra todo y vuelve a cargar los datos de ejemplo (solo lo usa el Administrador). */
    public void restablecer() throws Exception {
        servicio.vaciar();
        ejecutar("db/datos.sql");
    }

    private void ejecutar(String recurso) throws Exception {
        String sql = new String(new ClassPathResource(recurso).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.setEscapeProcessing(false);
            st.execute(sql);
        }
    }
}
