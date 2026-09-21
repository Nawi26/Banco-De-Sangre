package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.PruebaCompatibilidadDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarPruebaCompatibilidadRequest;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.PruebaCompatibilidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-12: prueba cruzada y RAI de unidades candidatas frente a una solicitud transfusional.
@RestController
@RequestMapping("/api/solicitudes/{solicitudId}/pruebas-cruzadas")
@AccesoClinico
public class PruebaCompatibilidadController {

    private final PruebaCompatibilidadService pruebaCompatibilidadService;

    public PruebaCompatibilidadController(PruebaCompatibilidadService pruebaCompatibilidadService) {
        this.pruebaCompatibilidadService = pruebaCompatibilidadService;
    }

    @GetMapping
    public List<PruebaCompatibilidadDTO> listar(@PathVariable Long solicitudId) {
        return pruebaCompatibilidadService.listarPorSolicitud(solicitudId);
    }

    @PostMapping
    @SoloPersonalBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public PruebaCompatibilidadDTO registrar(@PathVariable Long solicitudId,
                                              @Valid @RequestBody RegistrarPruebaCompatibilidadRequest request,
                                              Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return pruebaCompatibilidadService.registrar(solicitudId, request, usuarioId);
    }
}
