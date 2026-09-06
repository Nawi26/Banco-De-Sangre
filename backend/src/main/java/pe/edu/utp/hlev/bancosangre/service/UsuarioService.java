package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearUsuarioRequest;
import pe.edu.utp.hlev.bancosangre.dto.UsuarioAdminDTO;
import pe.edu.utp.hlev.bancosangre.model.Rol;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.RolRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioAdminDTO> listarMedicos() {
        return usuarioRepository.findTodosLosMedicos()
                .stream()
                .map(UsuarioAdminDTO::from)
                .toList();
    }

    @Transactional
    public UsuarioAdminDTO crearMedico(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByDniOrEmail(request.dni(), request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese DNI o correo.");
        }

        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Rol inválido."));

        Usuario usuario = new Usuario();
        usuario.setDni(request.dni());
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setEmail(request.email());
        usuario.setColegiatura(request.colegiatura());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setEstado(true);
        usuario.setRol(rol);

        return UsuarioAdminDTO.from(usuarioRepository.save(usuario));
    }
}
