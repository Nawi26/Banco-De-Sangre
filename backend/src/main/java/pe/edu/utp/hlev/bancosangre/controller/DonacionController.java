package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonacionDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.DonacionService;
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

    public DonacionController(DonacionService donacionService) {
        this.donacionService = donacionService;
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
}
