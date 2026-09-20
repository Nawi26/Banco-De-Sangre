package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.RegistrarTamizajeRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResolverDiscordanciaRequest;
import pe.edu.utp.hlev.bancosangre.dto.TamizajeSerologicoDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.security.SoloSupervisionBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.TamizajeSerologicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// RF-07/RF-08/RF-31: tamizaje serológico, bloqueo automático y firma electrónica reforzada.
@RestController
@RequestMapping("/api/donaciones/{donacionId}/tamizaje")
@SoloPersonalBancoSangre
public class TamizajeSerologicoController {

    private final TamizajeSerologicoService tamizajeSerologicoService;

    public TamizajeSerologicoController(TamizajeSerologicoService tamizajeSerologicoService) {
        this.tamizajeSerologicoService = tamizajeSerologicoService;
    }

    @GetMapping
    public TamizajeSerologicoDTO obtener(@PathVariable Long donacionId) {
        return tamizajeSerologicoService.obtenerPorDonacion(donacionId);
    }

    @PostMapping("/primera-digitacion")
    @ResponseStatus(HttpStatus.CREATED)
    public TamizajeSerologicoDTO primeraDigitacion(@PathVariable Long donacionId,
                                                     @Valid @RequestBody RegistrarTamizajeRequest request,
                                                     Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return tamizajeSerologicoService.primeraDigitacion(donacionId, request, usuarioId);
    }

    @PostMapping("/segunda-digitacion")
    public TamizajeSerologicoDTO segundaDigitacion(@PathVariable Long donacionId,
                                                     @Valid @RequestBody RegistrarTamizajeRequest request,
                                                     Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return tamizajeSerologicoService.segundaDigitacion(donacionId, request, usuarioId);
    }

    // Sólo el Jefe de Banco de Sangre (o Administrador) dirime marcadores discordantes.
    @PostMapping("/resolver-discordancia")
    @SoloSupervisionBancoSangre
    public TamizajeSerologicoDTO resolverDiscordancia(@PathVariable Long donacionId,
                                                        @Valid @RequestBody ResolverDiscordanciaRequest request,
                                                        Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return tamizajeSerologicoService.resolverDiscordancia(donacionId, request, usuarioId);
    }
}
