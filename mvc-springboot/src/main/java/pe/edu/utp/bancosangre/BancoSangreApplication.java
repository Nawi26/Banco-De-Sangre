package pe.edu.utp.bancosangre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Sistema Web de Gestion de Banco de Sangre (ISBT 128).
 * Hospital de Lima Este Vitarte - Banco de Sangre Tipo II.
 *
 * Esta etapa cubre la capa de presentacion (Vista + Controlador de navegacion).
 * El Modelo de dominio (entidades JPA, repositorios, servicios) se conecta en la
 * siguiente etapa del proyecto; por ahora las vistas usan datos de ejemplo en
 * JavaScript (ver static/js/app.js), tal como en la versión original.
 */
@SpringBootApplication
public class BancoSangreApplication {
    public static void main(String[] args) {
        SpringApplication.run(BancoSangreApplication.class, args);
    }
}
