package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.ActualizarEstadoSolicitudRequest;
import pe.edu.utp.hlev.bancosangre.dto.CrearSolicitudRequest;
import pe.edu.utp.hlev.bancosangre.dto.SolicitudDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloAdministrador;
import pe.edu.utp.hlev.bancosangre.security.SoloMedicoSolicitante;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// RBAC (RF-02): visibilidad clínica general, pero cada acción respeta el rol responsable.
@RestController
@RequestMapping("/api/solicitudes")
@AccesoClinico
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public List<SolicitudDTO> listar() {
        return solicitudService.listar();
    }

    // RF-11: sólo el Médico Solicitante registra la solicitud transfusional.
    @PostMapping
    @SoloMedicoSolicitante
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudDTO crear(@Valid @RequestBody CrearSolicitudRequest request, Authentication authentication) {
        Long medicoId = (Long) authentication.getPrincipal();
        return solicitudService.crear(request, medicoId);
    }

    // El banco de sangre es quien aprueba/rechaza/atiende la solicitud.
    @PatchMapping("/{id}/estado")
    @SoloPersonalBancoSangre
    public Map<String, Object> actualizarEstado(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoSolicitudRequest request) {
        solicitudService.actualizarEstado(id, request);
        return Map.of("exito", true);
    }

    @DeleteMapping("/{id}")
    @SoloAdministrador
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        solicitudService.eliminar(id);
    }
}
