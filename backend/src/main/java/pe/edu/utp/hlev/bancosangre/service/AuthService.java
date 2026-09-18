package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.LoginRequest;
import pe.edu.utp.hlev.bancosangre.dto.LoginResponse;
import pe.edu.utp.hlev.bancosangre.dto.UsuarioSesionDTO;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;
import pe.edu.utp.hlev.bancosangre.security.JwtService;
import pe.edu.utp.hlev.bancosangre.security.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class AuthService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    // RF-01: roles que requieren colegiatura profesional vigente (CMP/CTP) para operar.
    private static final Set<String> ROLES_CON_COLEGIATURA_OBLIGATORIA = Set.of(
            Roles.MEDICO_SOLICITANTE, Roles.TECNOLOGO_MEDICO, Roles.JEFE_BANCO_SANGRE
    );

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                        AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            LoginResponse respuesta = intentarLogin(request);
            auditoriaService.registrarEventoActual("LOGIN", respuesta.usuario().id(), request.identificador(),
                    respuesta.usuario().rol(), true, 200, "Inicio de sesión exitoso.");
            return respuesta;
        } catch (ApiException ex) {
            auditoriaService.registrarEventoActual("LOGIN", null, request.identificador(), null,
                    false, ex.getStatus().value(), ex.getMessage());
            throw ex;
        }
    }

    private LoginResponse intentarLogin(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByDniOrEmail(request.identificador())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Error: No existe una cuenta vinculada a este usuario."));

        if (Boolean.FALSE.equals(usuario.getEstado())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acceso denegado: Esta cuenta ha sido deshabilitada.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(ahora)) {
            long minutosRestantes = Math.max(1, ChronoUnit.MINUTES.between(ahora, usuario.getBloqueadoHasta()));
            throw new ApiException(HttpStatus.LOCKED,
                    "Cuenta bloqueada temporalmente por demasiados intentos fallidos. Intente nuevamente en " + minutosRestantes + " minuto(s).");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            registrarIntentoFallido(usuario);
            throw new ApiException(HttpStatus.UNAUTHORIZED, mensajeCredencialesInvalidas(usuario));
        }

        String rol = usuario.getRol().getNombre();
        if (ROLES_CON_COLEGIATURA_OBLIGATORIA.contains(rol) &&
                (usuario.getColegiatura() == null || usuario.getColegiatura().isBlank())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Acceso denegado: la cuenta no tiene registrada la colegiatura profesional (CMP/CTP) requerida para este rol.");
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
        String token = jwtService.generarToken(usuario.getId(), rol, nombreCompleto);

        return new LoginResponse(true, token, new UsuarioSesionDTO(usuario.getId(), nombreCompleto, rol));
    }

    private void registrarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plus(DURACION_BLOQUEO));
        }

        usuarioRepository.save(usuario);
    }

    private String mensajeCredencialesInvalidas(Usuario usuario) {
        int restantes = MAX_INTENTOS_FALLIDOS - usuario.getIntentosFallidos();
        if (restantes <= 0) {
            return "Credenciales incorrectas. Cuenta bloqueada temporalmente por " + DURACION_BLOQUEO.toMinutes() + " minutos.";
        }
        return "Credenciales incorrectas. Le queda(n) " + restantes + " intento(s) antes de que la cuenta se bloquee temporalmente.";
    }
}
