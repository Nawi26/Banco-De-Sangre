package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.EnviarMensajeRequest;
import pe.edu.utp.hlev.bancosangre.dto.MensajeDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.service.MensajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-48: mensajería interna banco de sangre <-> servicio asistencial, por solicitud.
@RestController
@RequestMapping("/api/solicitudes/{solicitudId}/mensajes")
@AccesoClinico
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    public List<MensajeDTO> listar(@PathVariable Long solicitudId) {
        return mensajeService.listarPorSolicitud(solicitudId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeDTO enviar(@PathVariable Long solicitudId, @Valid @RequestBody EnviarMensajeRequest request,
                              Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return mensajeService.enviar(solicitudId, request, usuarioId);
    }
}
