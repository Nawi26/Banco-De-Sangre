package pe.edu.utp.bancosangre.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Redirige la raiz "/" a "/login" usando el registro oficial de Spring MVC
 * (ViewControllerRegistry) en lugar de un metodo @GetMapping("/") en un
 * @Controller normal. Este registro tiene prioridad garantizada sobre el
 * manejador de "welcome page" / recursos estaticos que trae Spring Boot por
 * defecto, evitando el 404 "No static resource ." al entrar por
 * http://localhost:8080 sin ruta.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/login");
    }
}
