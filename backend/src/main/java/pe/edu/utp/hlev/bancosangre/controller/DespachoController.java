package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.ConfirmarDespachoRequest;
import pe.edu.utp.hlev.bancosangre.dto.DespachoDTO;
import pe.edu.utp.hlev.bancosangre.dto.IniciarDespachoRequest;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.DespachoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-13: doble verificación electrónica del despacho de hemocomponentes.
@RestController
@RequestMapping("/api/despachos")
@SoloPersonalBancoSangre
public class DespachoController {

    private final DespachoService despachoService;

    public DespachoController(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @GetMapping("/pendientes")
    public List<DespachoDTO> listarPendientes() {
        return despachoService.listarPendientes();
    }

    // Primera verificación: quien prepara la unidad para el despacho.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DespachoDTO iniciar(@Valid @RequestBody IniciarDespachoRequest request, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return despachoService.iniciar(request, usuarioId);
    }

    // Segunda verificación: un responsable distinto confirma y libera el despacho.
    @PostMapping("/{id}/confirmar")
    public DespachoDTO confirmar(@PathVariable Long id, @Valid @RequestBody ConfirmarDespachoRequest request,
                                  Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return despachoService.confirmar(id, request, usuarioId);
    }
}
