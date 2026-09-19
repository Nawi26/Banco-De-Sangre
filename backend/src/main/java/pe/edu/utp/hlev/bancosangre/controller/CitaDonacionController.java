package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CitaDonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearCitaRequest;
import pe.edu.utp.hlev.bancosangre.dto.ReprogramarCitaRequest;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.CitaDonacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-22: programación, reprogramación y recordatorio de citas de donación.
@RestController
@RequestMapping("/api/citas-donacion")
@SoloPersonalBancoSangre
public class CitaDonacionController {

    private final CitaDonacionService citaDonacionService;

    public CitaDonacionController(CitaDonacionService citaDonacionService) {
        this.citaDonacionService = citaDonacionService;
    }

    @GetMapping
    public List<CitaDonacionDTO> listar(@RequestParam(required = false) Long donanteId) {
        return donanteId != null ? citaDonacionService.listarPorDonante(donanteId) : citaDonacionService.listar();
    }

    // Recordatorio automático: citas pendientes dentro de las próximas `horas` horas (por defecto 48h).
    @GetMapping("/proximas")
    public List<CitaDonacionDTO> proximas(@RequestParam(defaultValue = "48") int horas) {
        return citaDonacionService.proximas(horas);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaDonacionDTO crear(@Valid @RequestBody CrearCitaRequest request) {
        return citaDonacionService.crear(request);
    }

    @PatchMapping("/{id}")
    public CitaDonacionDTO reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarCitaRequest request) {
        return citaDonacionService.reprogramar(id, request);
    }
}
