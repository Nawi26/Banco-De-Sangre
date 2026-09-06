package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.ActualizarEstadoSolicitudRequest;
import pe.edu.utp.hlev.bancosangre.dto.CrearSolicitudRequest;
import pe.edu.utp.hlev.bancosangre.dto.SolicitudDTO;
import pe.edu.utp.hlev.bancosangre.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public List<SolicitudDTO> listar() {
        return solicitudService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudDTO crear(@Valid @RequestBody CrearSolicitudRequest request, Authentication authentication) {
        Long medicoId = (Long) authentication.getPrincipal();
        return solicitudService.crear(request, medicoId);
    }

    @PatchMapping("/{id}/estado")
    public Map<String, Object> actualizarEstado(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoSolicitudRequest request) {
        solicitudService.actualizarEstado(id, request);
        return Map.of("exito", true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Administrador')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        solicitudService.eliminar(id);
    }
}
