package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.FraccionarDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.HemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.DonacionService;
import pe.edu.utp.hlev.bancosangre.service.FraccionamientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RBAC (RF-02): sólo personal del banco de sangre gestiona donaciones.
@RestController
@RequestMapping("/api/donaciones")
@SoloPersonalBancoSangre
public class DonacionController {

    private final DonacionService donacionService;
    private final FraccionamientoService fraccionamientoService;

    public DonacionController(DonacionService donacionService, FraccionamientoService fraccionamientoService) {
        this.donacionService = donacionService;
        this.fraccionamientoService = fraccionamientoService;
    }

    @GetMapping
    public List<DonacionDTO> listar() {
        return donacionService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonacionDTO crear(@Valid @RequestBody CrearDonacionRequest request) {
        return donacionService.crear(request);
    }

    // RF-06: fracciona la bolsa de sangre total en los hemocomponentes indicados.
    @PostMapping("/{id}/fraccionamiento")
    @ResponseStatus(HttpStatus.CREATED)
    public List<HemocomponenteDTO> fraccionar(@PathVariable Long id, @Valid @RequestBody FraccionarDonacionRequest request) {
        return fraccionamientoService.fraccionar(id, request);
    }

    @GetMapping("/{id}/hemocomponentes")
    public List<HemocomponenteDTO> listarHemocomponentes(@PathVariable Long id) {
        return fraccionamientoService.listarPorDonacion(id);
    }
}
