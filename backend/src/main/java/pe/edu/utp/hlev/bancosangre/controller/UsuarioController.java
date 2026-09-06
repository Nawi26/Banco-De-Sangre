package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearUsuarioRequest;
import pe.edu.utp.hlev.bancosangre.dto.UsuarioAdminDTO;
import pe.edu.utp.hlev.bancosangre.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Exclusivo Administrador (RBAC aplicado a nivel de método con @PreAuthorize)
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAuthority('Administrador')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioAdminDTO> listarMedicos() {
        return usuarioService.listarMedicos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crearMedico(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioAdminDTO creado = usuarioService.crearMedico(request);
        return Map.of("exito", true, "usuario", creado);
    }
}
