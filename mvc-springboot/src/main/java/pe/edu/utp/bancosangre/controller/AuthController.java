package pe.edu.utp.bancosangre.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.utp.bancosangre.service.BancoSangreService;
import pe.edu.utp.bancosangre.model.Rol;

import java.util.Map;

/**
 * Controlador de acceso (RF-01: inicio de sesion seguro con DNI/correo, contrasena
 * cifrada y rol asignado a la cuenta).
 *
 * El acceso se valida contra la tabla usuarios (RF-01). El rol de la cuenta se guarda
 * en la sesion HTTP, que es lo que usa {@link AppController} para aplicar el RBAC (RF-02)
 * del lado del servidor.
 */
@Controller
public class AuthController {

    private final BancoSangreService servicio;

    public AuthController(BancoSangreService servicio) {
        this.servicio = servicio;
    }

    public static final String SESSION_ROL = "rolActual";
    public static final String SESSION_USUARIO = "usuarioActual";

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute(SESSION_ROL) != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /**
     * Inicio de sesión real: valida el formato del usuario y la contraseña y los comprueba contra la
     * tabla usuarios (contraseña cifrada con bcrypt). El rol sale de la cuenta, no de lo que elija el navegador.
     */
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam("usuario") String usuario,
                                 @RequestParam(value = "clave", defaultValue = "") String clave,
                                 HttpSession session, Model model) {
        String u = usuario == null ? "" : usuario.trim().toLowerCase();
        String error = null;
        if (!(u.matches("^\\d{8}$") || u.matches("^[a-z0-9._-]+@hlev\\.gob\\.pe$"))) {
            error = "Ingresa tu DNI (8 dígitos) o tu correo institucional @hlev.gob.pe";
        } else if (clave.length() < 6) {
            error = "La contraseña debe tener al menos 6 caracteres";
        }
        Map<String, Object> cuenta = null;
        if (error == null) {
            cuenta = servicio.autenticar(u, clave);
            if (cuenta == null) {
                error = "Usuario o contraseña incorrectos, o la cuenta está desactivada";
            }
        }
        if (error != null) {
            model.addAttribute("errorAcceso", error);
            return "login";
        }
        String correo = String.valueOf(cuenta.get("correo"));
        session.setAttribute(SESSION_ROL, Rol.desdeEtiqueta(String.valueOf(cuenta.get("rol"))));
        session.setAttribute(SESSION_USUARIO, correo);
        try { servicio.registrarAcceso(correo); } catch (Exception ignorada) { /* el acceso no debe fallar por esto */ }
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
