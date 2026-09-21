package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CamaraAlmacenamientoDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearCamaraRequest;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarTemperaturaRequest;
import pe.edu.utp.hlev.bancosangre.dto.RegistroTemperaturaDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.CamaraAlmacenamientoService;
import pe.edu.utp.hlev.bancosangre.service.TemperaturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-09/RF-24: cámaras de refrigeración/congelación y su monitoreo de temperatura.
@RestController
@RequestMapping("/api/camaras")
@SoloPersonalBancoSangre
public class CamaraAlmacenamientoController {

    private final CamaraAlmacenamientoService camaraAlmacenamientoService;
    private final TemperaturaService temperaturaService;

    public CamaraAlmacenamientoController(CamaraAlmacenamientoService camaraAlmacenamientoService,
                                           TemperaturaService temperaturaService) {
        this.camaraAlmacenamientoService = camaraAlmacenamientoService;
        this.temperaturaService = temperaturaService;
    }

    @GetMapping
    public List<CamaraAlmacenamientoDTO> listar() {
        return camaraAlmacenamientoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CamaraAlmacenamientoDTO crear(@Valid @RequestBody CrearCamaraRequest request) {
        return camaraAlmacenamientoService.crear(request);
    }

    // Punto de integración con sensores (o registro manual) de temperatura.
    @PostMapping("/{id}/temperaturas")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroTemperaturaDTO registrarTemperatura(@PathVariable Long id,
                                                         @Valid @RequestBody RegistrarTemperaturaRequest request,
                                                         Authentication authentication) {
        Long usuarioId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return temperaturaService.registrar(id, request, usuarioId);
    }

    @GetMapping("/{id}/temperaturas")
    public List<RegistroTemperaturaDTO> listarTemperaturas(@PathVariable Long id) {
        return temperaturaService.listarPorCamara(id);
    }

    // RF-14: alertas de temperatura fuera de rango en las últimas `horas` horas (por defecto 24h).
    @GetMapping("/temperaturas/alertas")
    public List<RegistroTemperaturaDTO> alertasTemperatura(@RequestParam(defaultValue = "24") int horas) {
        return temperaturaService.alertasRecientes(horas);
    }
}
