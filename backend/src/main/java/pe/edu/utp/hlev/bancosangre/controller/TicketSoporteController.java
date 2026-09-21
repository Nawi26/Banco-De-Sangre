package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearTicketSoporteRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResponderTicketSoporteRequest;
import pe.edu.utp.hlev.bancosangre.dto.TicketSoporteDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloAdministrador;
import pe.edu.utp.hlev.bancosangre.service.TicketSoporteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-33: mesa de ayuda interna para incidencias técnicas.
@RestController
@RequestMapping("/api/tickets-soporte")
@AccesoClinico
public class TicketSoporteController {

    private final TicketSoporteService ticketSoporteService;

    public TicketSoporteController(TicketSoporteService ticketSoporteService) {
        this.ticketSoporteService = ticketSoporteService;
    }

    @GetMapping("/propios")
    public List<TicketSoporteDTO> listarPropios(Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return ticketSoporteService.listarPropios(usuarioId);
    }

    @GetMapping
    @SoloAdministrador
    public List<TicketSoporteDTO> listarTodos() {
        return ticketSoporteService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketSoporteDTO crear(@Valid @RequestBody CrearTicketSoporteRequest request, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return ticketSoporteService.crear(request, usuarioId);
    }

    @PatchMapping("/{id}")
    @SoloAdministrador
    public TicketSoporteDTO responder(@PathVariable Long id, @Valid @RequestBody ResponderTicketSoporteRequest request,
                                       Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return ticketSoporteService.responder(id, request, usuarioId);
    }
}
