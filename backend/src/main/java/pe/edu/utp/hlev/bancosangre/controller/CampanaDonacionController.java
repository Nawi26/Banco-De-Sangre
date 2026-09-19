package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CampanaDonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearCampanaRequest;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.CampanaDonacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-23/RF-35: campañas externas de captación de donantes.
@RestController
@RequestMapping("/api/campanas")
@SoloPersonalBancoSangre
public class CampanaDonacionController {

    private final CampanaDonacionService campanaDonacionService;

    public CampanaDonacionController(CampanaDonacionService campanaDonacionService) {
        this.campanaDonacionService = campanaDonacionService;
    }

    @GetMapping
    public List<CampanaDonacionDTO> listar() {
        return campanaDonacionService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampanaDonacionDTO crear(@Valid @RequestBody CrearCampanaRequest request) {
        return campanaDonacionService.crear(request);
    }
}
