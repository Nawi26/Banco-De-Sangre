package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

/**
 * RF-31: validación reforzada del responsable en etapas críticas (tamizaje,
 * fraccionamiento, despacho, etc.) mediante reautenticación con su propia
 * contraseña, a modo de firma electrónica simple.
 */
@Service
public class FirmaElectronicaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public FirmaElectronicaService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** @return el usuario ya validado, para registrar como responsable de la acción. */
    public Usuario validar(Long usuarioId, String clave) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no válido."));

        if (clave == null || !passwordEncoder.matches(clave, usuario.getPassword())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Firma electrónica inválida: la contraseña ingresada no corresponde al usuario autenticado.");
        }

        return usuario;
    }
}
