package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearReservaQuirurgicaRequest;
import pe.edu.utp.hlev.bancosangre.dto.ReservaQuirurgicaDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloMedicoSolicitante;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.ReservaQuirurgicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// RF-25: reserva quirúrgica de hemocomponentes con liberación automática.
@RestController
@RequestMapping("/api/reservas-quirurgicas")
@AccesoClinico
public class ReservaQuirurgicaController {

    private final ReservaQuirurgicaService reservaQuirurgicaService;

    public ReservaQuirurgicaController(ReservaQuirurgicaService reservaQuirurgicaService) {
        this.reservaQuirurgicaService = reservaQuirurgicaService;
    }

    @GetMapping
    public List<ReservaQuirurgicaDTO> listar() {
        return reservaQuirurgicaService.listar();
    }

    @PostMapping
    @SoloMedicoSolicitante
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaQuirurgicaDTO reservar(@Valid @RequestBody CrearReservaQuirurgicaRequest request, Authentication authentication) {
        Long medicoId = (Long) authentication.getPrincipal();
        return reservaQuirurgicaService.reservar(request, medicoId);
    }

    @PatchMapping("/{id}/liberar")
    @SoloPersonalBancoSangre
    public Map<String, Object> liberar(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        reservaQuirurgicaService.liberar(id, usuarioId);
        return Map.of("exito", true);
    }

    @PatchMapping("/{id}/utilizada")
    @SoloPersonalBancoSangre
    public Map<String, Object> marcarUtilizada(@PathVariable Long id) {
        reservaQuirurgicaService.marcarUtilizada(id);
        return Map.of("exito", true);
    }
}
