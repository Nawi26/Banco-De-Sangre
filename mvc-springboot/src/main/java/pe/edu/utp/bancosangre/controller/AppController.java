package pe.edu.utp.bancosangre.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.bancosangre.service.BancoSangreService;
import pe.edu.utp.bancosangre.model.Rol;
import pe.edu.utp.bancosangre.model.Vista;

/**
 * Controlador de navegacion entre las 12 pantallas del sistema.
 *
 * Cada @GetMapping es una ruta real (en la versión SPA original eran secciones
 * togglable-por-JS dentro de una sola pagina). Aqui la navegacion la resuelve el
 * servidor: el Controlador valida el RBAC (RF-02) contra el rol guardado en sesion
 * y decide que Vista (plantilla Thymeleaf) renderizar, exactamente como hacia
 * showView()/applyRoleVisibility() en el JavaScript de la versión original, pero ahora en Java.
 */
@Controller
public class AppController {

    private final BancoSangreService servicio;

    public AppController(BancoSangreService servicio) {
        this.servicio = servicio;
    }

    /**
     * Modo movil: cualquiera de las 12 rutas acepta ?movil=1 y se dibuja sin
     * menu lateral y en una sola columna (ver la clase CSS .app-movil). Es lo
     * que hace que la pantalla "Aplicacion movil" pueda mostrar la aplicacion
     * REAL dentro del celular, en vez de una maqueta con datos aparte.
     */
    @ModelAttribute("modoMovil")
    public boolean modoMovil(@RequestParam(value = "movil", defaultValue = "false") boolean movil) {
        return movil;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("dashboard", session, model, ra);
    }

    @GetMapping("/inventario")
    public String inventario(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("inventario", session, model, ra);
    }

    @GetMapping("/donantes")
    public String donantes(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("donantes", session, model, ra);
    }

    @GetMapping("/pacientes")
    public String pacientes(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("pacientes", session, model, ra);
    }

    @GetMapping("/etiquetado")
    public String etiquetado(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("etiquetado", session, model, ra);
    }

    @GetMapping("/solicitudes")
    public String solicitudes(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("solicitudes", session, model, ra);
    }

    @GetMapping("/despacho")
    public String despacho(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("despacho", session, model, ra);
    }

    @GetMapping("/interhospitalario")
    public String interhospitalario(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("interhospitalario", session, model, ra);
    }

    @GetMapping("/movil")
    public String movil(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("movil", session, model, ra);
    }

    @GetMapping("/hemovigilancia")
    public String hemovigilancia(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("hemovigilancia", session, model, ra);
    }

    @GetMapping("/reportes")
    public String reportes(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("reportes", session, model, ra);
    }

    @GetMapping("/auditoria")
    public String auditoria(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("auditoria", session, model, ra);
    }

    @GetMapping("/administracion")
    public String administracion(HttpSession session, Model model, RedirectAttributes ra) {
        return renderVista("administracion", session, model, ra);
    }

    /**
     * Logica compartida por las 12 rutas:
     *  1. Exige sesion iniciada (si no, RF-01: redirige a /login).
     *  2. Aplica el RBAC de RF-02: si el rol no tiene acceso a la vista pedida,
     *     redirige a /dashboard con un mensaje (equivalente al toast() de la versión original).
     *  3. Carga en el Modelo el catalogo de vistas (para el menu lateral + topbar)
     *     y cual esta activa, y devuelve el nombre de la plantilla Thymeleaf.
     */
    private String renderVista(String clave, HttpSession session, Model model, RedirectAttributes ra) {
        Rol rol = (Rol) session.getAttribute(AuthController.SESSION_ROL);
        if (rol == null) {
            return "redirect:/login";
        }
        if (!rol.puedeVer(clave)) {
            ra.addFlashAttribute("errorAcceso",
                    "Tu rol (" + rol.getEtiqueta() + ") no tiene acceso a esa vista");
            return "redirect:/dashboard";
        }

        model.addAttribute("vistaActual", clave);
        model.addAttribute("info", Vista.CATALOGO.get(clave));
        model.addAttribute("catalogo", Vista.CATALOGO.values());
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", session.getAttribute(AuthController.SESSION_USUARIO));
        cargarDatos(clave, model);
        return clave;
    }

    /**
     * Carga en el Modelo los datos que cada Vista necesita, leidos de
     * {@link BancoSangreService} (que usa los DAO sobre PostgreSQL). Cada plantilla los usa para inicializar su propio estado en
     * pantalla, en vez de traerlos ya escritos a mano en JavaScript.
     */
    private void cargarDatos(String clave, Model model) {
        switch (clave) {
            case "dashboard" -> {
                model.addAttribute("aboGroups", servicio.listar("stock"));
                model.addAttribute("alertas", servicio.listar("alertas"));
            }
            case "inventario" -> {
                model.addAttribute("inventario", servicio.listar("inventario"));
                model.addAttribute("temperaturas", servicio.listar("temperaturas"));
                // Cada unidad muestra su donante de origen y su tamizaje
                model.addAttribute("donantes", servicio.listar("donantes"));
                model.addAttribute("serologia", servicio.listar("serologia"));
            }
            case "donantes" -> {
                model.addAttribute("donantes", servicio.listar("donantes"));
                // El tamizaje serologico trabaja sobre las unidades en cuarentena
                model.addAttribute("inventario", servicio.listar("inventario"));
                model.addAttribute("serologia", servicio.listar("serologia"));
            }
            case "solicitudes" -> {
                model.addAttribute("solicitudes", servicio.listar("solicitudes"));
                // Pacientes registrados: sugerencias de historia clínica al escribir una solicitud
                model.addAttribute("pacientes", servicio.listar("pacientes"));
                // Las pruebas de compatibilidad se cruzan contra una unidad del inventario
                model.addAttribute("inventario", servicio.listar("inventario"));
                model.addAttribute("pruebas", servicio.listar("pruebas"));
            }
            case "despacho" -> {
                // El escaneo de hemocomponente y de pulsera necesitan ambas listas
                model.addAttribute("inventario", servicio.listar("inventario"));
                model.addAttribute("solicitudes", servicio.listar("solicitudes"));
            }
            case "hemovigilancia" -> {
                model.addAttribute("hemovigilancia", servicio.listar("hemovigilancia"));
                // El registro de un evento se vincula al DIN de una unidad despachada
                model.addAttribute("inventario", servicio.listar("inventario"));
            }
            case "interhospitalario" -> {
                model.addAttribute("intercambios", servicio.listar("intercambios"));
                model.addAttribute("establecimientos", servicio.listar("establecimientos"));
                model.addAttribute("inventario", servicio.listar("inventario"));
            }
            case "pacientes" -> model.addAttribute("pacientes", servicio.listar("pacientes"));
            case "administracion" -> model.addAttribute("usuarios", servicio.listar("usuarios"));
            case "auditoria" -> model.addAttribute("auditLog", servicio.listar("auditoria"));
            case "reportes" -> {
                // Cada reporte se arma con los datos reales del sistema
                model.addAttribute("inventario", servicio.listar("inventario"));
                model.addAttribute("donantes", servicio.listar("donantes"));
                model.addAttribute("solicitudes", servicio.listar("solicitudes"));
                model.addAttribute("hemovigilancia", servicio.listar("hemovigilancia"));
                model.addAttribute("auditLog", servicio.listar("auditoria"));
            }
            case "movil" -> {
                // La app móvil espeja los mismos datos reales del panel web
                // (stock crítico, alertas y solicitudes activas), no datos aparte.
                model.addAttribute("aboGroups", servicio.listar("stock"));
                model.addAttribute("alertas", servicio.listar("alertas"));
                model.addAttribute("solicitudes", servicio.listar("solicitudes"));
            }
            default -> { /* sin datos de ejemplo asociados */ }
        }
    }
}
