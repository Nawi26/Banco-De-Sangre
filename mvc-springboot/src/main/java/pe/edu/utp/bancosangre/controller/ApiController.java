package pe.edu.utp.bancosangre.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.bancosangre.ValidadorDatos;
import pe.edu.utp.bancosangre.db.InicializadorBD;
import pe.edu.utp.bancosangre.service.BancoSangreService;
import pe.edu.utp.bancosangre.model.Rol;

import java.util.Map;

/**
 * Puntos de entrada que usa la pantalla para guardar en la base de datos.
 * Exigen sesion iniciada; el usuario y el rol salen de la sesion, no del navegador.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final BancoSangreService servicio;
    private final InicializadorBD inicializador;

    public ApiController(BancoSangreService servicio, InicializadorBD inicializador) {
        this.servicio = servicio;
        this.inicializador = inicializador;
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<Map<String, Object>> sincronizar(@RequestBody String cambios, HttpSession session) {
        Rol rol = (Rol) session.getAttribute(AuthController.SESSION_ROL);
        if (rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "error", "Sesion vencida: vuelve a iniciar sesion"));
        }
        String errorDatos = ValidadorDatos.validar(cambios);
        if (errorDatos != null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", errorDatos));
        }
        try {
            servicio.sincronizar(cambios, String.valueOf(session.getAttribute(AuthController.SESSION_USUARIO)), rol.getEtiqueta());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            Throwable causa = e;
            while (causa.getCause() != null) causa = causa.getCause();
            String msg = String.valueOf(causa.getMessage()).split("\n")[0].replaceFirst("^ERROR:\\s*", "");
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", msg));
        }
    }

    /** Lectura de colecciones ya guardadas (la app movil las usa para enterarse de novedades). */
    @GetMapping("/listar/{coleccion}")
    public ResponseEntity<Object> listar(@PathVariable String coleccion, HttpSession session) {
        if (session.getAttribute(AuthController.SESSION_ROL) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!java.util.Set.of("alertas", "solicitudes").contains(coleccion)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(servicio.listar(coleccion));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, Object>> restablecer(HttpSession session) {
        Rol rol = (Rol) session.getAttribute(AuthController.SESSION_ROL);
        if (rol == null || rol != Rol.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("ok", false, "error", "Solo el Administrador puede restablecer los datos"));
        }
        try {
            inicializador.restablecer();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "error", String.valueOf(e.getMessage())));
        }
    }
}
