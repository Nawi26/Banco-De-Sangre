package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.*;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.security.SoloSupervisionBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.ConvenioInterinstitucionalService;
import pe.edu.utp.hlev.bancosangre.service.IntercambioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// RBAC (RF-02): visibilidad de red (RF-19) abierta a todo el personal clínico;
// crear/gestionar intercambios (RF-18) queda restringido al banco de sangre.
@RestController
@RequestMapping("/api")
@AccesoClinico
public class IntercambioController {

    private final IntercambioService intercambioService;
    private final ConvenioInterinstitucionalService convenioInterinstitucionalService;

    public IntercambioController(IntercambioService intercambioService,
                                  ConvenioInterinstitucionalService convenioInterinstitucionalService) {
        this.intercambioService = intercambioService;
        this.convenioInterinstitucionalService = convenioInterinstitucionalService;
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
    @SoloPersonalBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crearIntercambio(@Valid @RequestBody CrearIntercambioRequest request) {
        Long id = intercambioService.crearIntercambio(request);
        return Map.of("exito", true, "id", id);
    }

    @PatchMapping("/intercambios/{id}/estado")
    @SoloPersonalBancoSangre
    public Map<String, Object> actualizarEstado(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoIntercambioRequest request) {
        intercambioService.actualizarEstado(id, request);
        return Map.of("exito", true);
    }

    // RF-40: devolución de una unidad prestada, con actualización automática del inventario de origen.
    @PostMapping("/intercambios/{id}/devolucion")
    @SoloPersonalBancoSangre
    public Map<String, Object> registrarDevolucion(@PathVariable Long id) {
        intercambioService.registrarDevolucion(id);
        return Map.of("exito", true);
    }

    // RF-45: convenios y contratos interinstitucionales vigentes.
    @GetMapping("/convenios")
    public List<ConvenioInterinstitucionalDTO> listarConvenios() {
        return convenioInterinstitucionalService.listar();
    }

    @PostMapping("/convenios")
    @SoloSupervisionBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public ConvenioInterinstitucionalDTO crearConvenio(@Valid @RequestBody CrearConvenioRequest request) {
        return convenioInterinstitucionalService.crear(request);
    }

    @PatchMapping("/convenios/{id}/estado")
    @SoloSupervisionBancoSangre
    public Map<String, Object> actualizarEstadoConvenio(@PathVariable Long id, @Valid @RequestBody ActualizarEstadoConvenioRequest request) {
        convenioInterinstitucionalService.actualizarEstado(id, request.estado());
        return Map.of("exito", true);
    }
}
