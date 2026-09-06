package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.*;
import pe.edu.utp.hlev.bancosangre.service.IntercambioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IntercambioController {

    private final IntercambioService intercambioService;

    public IntercambioController(IntercambioService intercambioService) {
        this.intercambioService = intercambioService;
    }

    @GetMapping("/hospitales")
    public List<HospitalDTO> listarHospitales() {
        return intercambioService.listarHospitales();
    }

    @GetMapping("/intercambios")
    public List<IntercambioDTO> listarIntercambios() {
        return intercambioService.listarIntercambios();
    }

    @PostMapping("/intercambios")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crearIntercambio(@Valid @RequestBody CrearIntercambioRequest request) {
        Long id = intercambioService.crearIntercambio(request);
        return Map.of("exito", true, "id", id);
    }

    @PatchMapping("/intercambios/{id}/estado")
    public Map<String, Object> actualizarEstado(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoIntercambioRequest request) {
        intercambioService.actualizarEstado(id, request);
        return Map.of("exito", true);
    }
}
