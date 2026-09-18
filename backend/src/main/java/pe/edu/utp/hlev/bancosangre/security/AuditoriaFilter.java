package pe.edu.utp.hlev.bancosangre.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.utp.hlev.bancosangre.service.AuditoriaService;

import java.io.IOException;
import java.util.Set;

/**
 * RF-17 / RNF-04: audita automáticamente toda acción que modifica estado (POST/PUT/PATCH/DELETE).
 * Se registra en el filtro de seguridad DESPUÉS de JwtAuthenticationFilter (ver SecurityConfig),
 * de modo que el usuario/rol autenticado ya está disponible en el SecurityContext.
 * El login se excluye aquí porque AuthService lo audita explícitamente con más detalle.
 */
@Component
public class AuditoriaFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_AUDITABLES = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditoriaService auditoriaService;

    public AuditoriaFilter(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            String metodo = request.getMethod();
            String uri = request.getRequestURI();
            boolean esLogin = "/api/login".equals(uri);

            if (METODOS_AUDITABLES.contains(metodo) && !esLogin) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                Long usuarioId = null;
                String rol = null;

                if (authentication != null && authentication.getPrincipal() instanceof Long id) {
                    usuarioId = id;
                    rol = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .findFirst()
                            .orElse(null);
                }

                int status = response.getStatus();
                auditoriaService.registrar(
                        usuarioId, null, rol, metodo, metodo + " " + uri, uri,
                        AuditoriaService.obtenerIp(request), status, status < 400, null
                );
            }
        }
    }
}
