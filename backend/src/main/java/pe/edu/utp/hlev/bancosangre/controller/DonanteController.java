package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonanteRequest;
import pe.edu.utp.hlev.bancosangre.dto.ConsentimientoInformadoDTO;
import pe.edu.utp.hlev.bancosangre.dto.CuestionarioTriajeRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonanteDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarConsentimientoRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResultadoTriajeDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.AuditoriaService;
import pe.edu.utp.hlev.bancosangre.service.ConsentimientoService;
import pe.edu.utp.hlev.bancosangre.service.DonanteService;
import pe.edu.utp.hlev.bancosangre.service.TriajeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RBAC (RF-02): sólo personal del banco de sangre gestiona donantes.
@RestController
@RequestMapping("/api/donantes")
@SoloPersonalBancoSangre
public class DonanteController {

    private final DonanteService donanteService;
    private final TriajeService triajeService;
    private final ConsentimientoService consentimientoService;

    public DonanteController(DonanteService donanteService, TriajeService triajeService,
                              ConsentimientoService consentimientoService) {
        this.donanteService = donanteService;
        this.triajeService = triajeService;
        this.consentimientoService = consentimientoService;
    }

    @GetMapping
    public List<DonanteDTO> listar() {
        return donanteService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crear(@Valid @RequestBody CrearDonanteRequest request) {
        return donanteService.crear(request);
    }

    // RF-03/RF-04: evalúa el cuestionario de triaje y determina aptitud/diferimiento de inmediato.
    @PostMapping("/{id}/triaje")
    public ResultadoTriajeDTO evaluarTriaje(@PathVariable Long id, @Valid @RequestBody CuestionarioTriajeRequest request,
                                             Authentication authentication) {
        Long evaluadorId = (Long) authentication.getPrincipal();
        return triajeService.evaluar(id, request, evaluadorId);
    }

    // RF-38: consentimiento informado digital previo a la extracción.
    @PostMapping("/{id}/consentimiento")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsentimientoInformadoDTO registrarConsentimiento(@PathVariable Long id,
                                                                @Valid @RequestBody RegistrarConsentimientoRequest request,
                                                                Authentication authentication,
                                                                HttpServletRequest httpServletRequest) {
        Long usuarioId = (Long) authentication.getPrincipal();
        String ip = AuditoriaService.obtenerIp(httpServletRequest);
        return consentimientoService.registrar(id, request, usuarioId, ip);
    }
}
