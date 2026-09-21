package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.EventoAdversoDonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.EventoAdversoTransfusionalDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarEventoAdversoDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarEventoAdversoTransfusionalRequest;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.EventoAdversoDonacionService;
import pe.edu.utp.hlev.bancosangre.service.EventoAdversoTransfusionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RF-15/RF-43: hemovigilancia — eventos adversos de la transfusión y de la donación.
@RestController
@AccesoClinico
public class HemovigilanciaController {

    private final EventoAdversoTransfusionalService eventoAdversoTransfusionalService;
    private final EventoAdversoDonacionService eventoAdversoDonacionService;

    public HemovigilanciaController(EventoAdversoTransfusionalService eventoAdversoTransfusionalService,
                                     EventoAdversoDonacionService eventoAdversoDonacionService) {
        this.eventoAdversoTransfusionalService = eventoAdversoTransfusionalService;
        this.eventoAdversoDonacionService = eventoAdversoDonacionService;
    }

    // RF-15: cualquier rol clínico puede detectar y reportar una reacción transfusional.
    @GetMapping("/api/hemovigilancia/transfusiones")
    public List<EventoAdversoTransfusionalDTO> listarEventosTransfusionales() {
        return eventoAdversoTransfusionalService.listarTodos();
    }

    @GetMapping("/api/transfusiones/{transfusionId}/eventos-adversos")
    public List<EventoAdversoTransfusionalDTO> listarPorTransfusion(@PathVariable Long transfusionId) {
        return eventoAdversoTransfusionalService.listarPorTransfusion(transfusionId);
    }

    @PostMapping("/api/transfusiones/{transfusionId}/eventos-adversos")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoAdversoTransfusionalDTO registrarEventoTransfusional(@PathVariable Long transfusionId,
                                                                       @Valid @RequestBody RegistrarEventoAdversoTransfusionalRequest request,
                                                                       Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return eventoAdversoTransfusionalService.registrar(transfusionId, request, usuarioId);
    }

    // RF-43: solo personal del banco de sangre presencia y reporta eventos durante la extracción.
    @GetMapping("/api/hemovigilancia/donaciones")
    @SoloPersonalBancoSangre
    public List<EventoAdversoDonacionDTO> listarEventosDonacion() {
        return eventoAdversoDonacionService.listarTodos();
    }

    @GetMapping("/api/donaciones/{donacionId}/eventos-adversos")
    @SoloPersonalBancoSangre
    public List<EventoAdversoDonacionDTO> listarPorDonacion(@PathVariable Long donacionId) {
        return eventoAdversoDonacionService.listarPorDonacion(donacionId);
    }

    @PostMapping("/api/donaciones/{donacionId}/eventos-adversos")
    @SoloPersonalBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public EventoAdversoDonacionDTO registrarEventoDonacion(@PathVariable Long donacionId,
                                                             @Valid @RequestBody RegistrarEventoAdversoDonacionRequest request,
                                                             Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return eventoAdversoDonacionService.registrar(donacionId, request, usuarioId);
    }
}
