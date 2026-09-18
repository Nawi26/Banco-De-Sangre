package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonanteRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonanteDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.DonanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RBAC (RF-02): sólo personal del banco de sangre gestiona donantes.
@RestController
@RequestMapping("/api/donantes")
@SoloPersonalBancoSangre
public class DonanteController {

    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService) {
        this.donanteService = donanteService;
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
}
