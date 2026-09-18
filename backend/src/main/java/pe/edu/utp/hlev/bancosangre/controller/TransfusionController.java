package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearTransfusionRequest;
import pe.edu.utp.hlev.bancosangre.dto.TransfusionDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.TransfusionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfusiones")
@AccesoClinico
public class TransfusionController {

    private final TransfusionService transfusionService;

    public TransfusionController(TransfusionService transfusionService) {
        this.transfusionService = transfusionService;
    }

    @GetMapping
    public List<TransfusionDTO> listar() {
        return transfusionService.listar();
    }

    // RF-12/RF-13: el tecnólogo (o jefe) ejecuta la prueba cruzada y el despacho.
    @PostMapping
    @SoloPersonalBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public TransfusionDTO crear(@Valid @RequestBody CrearTransfusionRequest request, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        String rol = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        return transfusionService.crear(request, usuarioId, rol);
    }
}
