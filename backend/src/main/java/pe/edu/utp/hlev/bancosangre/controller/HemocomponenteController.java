package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.AsignarUbicacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.DescartarHemocomponenteRequest;
import pe.edu.utp.hlev.bancosangre.dto.DescarteHemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.HemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.HistorialHemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.IsbtEtiquetaDTO;
import pe.edu.utp.hlev.bancosangre.dto.LeerCodigoRequest;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.DescarteService;
import pe.edu.utp.hlev.bancosangre.service.HemocomponenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// RF-05: consulta, etiquetado (ISBT 128) y lectura de códigos de hemocomponentes.
@RestController
@RequestMapping("/api/hemocomponentes")
@AccesoClinico
public class HemocomponenteController {

    private final HemocomponenteService hemocomponenteService;
    private final DescarteService descarteService;

    public HemocomponenteController(HemocomponenteService hemocomponenteService, DescarteService descarteService) {
        this.hemocomponenteService = hemocomponenteService;
        this.descarteService = descarteService;
    }

    @GetMapping("/{id}")
    public HemocomponenteDTO obtener(@PathVariable Long id) {
        return hemocomponenteService.obtener(id);
    }

    @GetMapping("/{id}/etiqueta")
    public IsbtEtiquetaDTO obtenerEtiqueta(@PathVariable Long id) {
        return hemocomponenteService.obtenerEtiqueta(id);
    }

    // RF-30: historial completo del ciclo de vida de la unidad.
    @GetMapping("/{id}/historial")
    public HistorialHemocomponenteDTO obtenerHistorial(@PathVariable Long id) {
        return hemocomponenteService.obtenerHistorial(id);
    }

    // RF-29: descarte de la unidad con motivo y evidencia fotográfica.
    @PostMapping("/{id}/descarte")
    @SoloPersonalBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public DescarteHemocomponenteDTO descartar(@PathVariable Long id, @Valid @RequestBody DescartarHemocomponenteRequest request,
                                                Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return descarteService.descartar(id, request, usuarioId);
    }

    // RF-09: asigna la unidad a una cámara de refrigeración/congelación.
    @PatchMapping("/{id}/ubicacion")
    @SoloPersonalBancoSangre
    public HemocomponenteDTO asignarUbicacion(@PathVariable Long id, @Valid @RequestBody AsignarUbicacionRequest request) {
        return hemocomponenteService.asignarUbicacion(id, request);
    }

    // Lectura óptica: el escaneo ocurre en el cliente; aquí se valida y resuelve el código leído.
    @PostMapping("/leer")
    @SoloPersonalBancoSangre
    public IsbtEtiquetaDTO leerCodigo(@Valid @RequestBody LeerCodigoRequest request) {
        return hemocomponenteService.leerCodigo(request.codigo());
    }
}
