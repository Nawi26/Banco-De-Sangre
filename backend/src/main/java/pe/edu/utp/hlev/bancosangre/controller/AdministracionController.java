package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.*;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.security.SoloAdministrador;
import pe.edu.utp.hlev.bancosangre.security.SoloPersonalBancoSangre;
import pe.edu.utp.hlev.bancosangre.security.SoloSupervisionBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-34/RF-39/RF-42/RF-47/RF-49: operación y administración del sistema.
 * Sin restricción a nivel de clase porque cada sub-recurso tiene su propia audiencia
 * (desde "cualquier rol clínico" en protocolos vigentes, hasta "sólo Administrador" en
 * respaldos y disponibilidad).
 */
@RestController
public class AdministracionController {

    private final TurnoPersonalService turnoPersonalService;
    private final MantenimientoEquipoService mantenimientoEquipoService;
    private final ProtocoloClinicoService protocoloClinicoService;
    private final RespaldoService respaldoService;
    private final DisponibilidadService disponibilidadService;

    public AdministracionController(TurnoPersonalService turnoPersonalService,
                                     MantenimientoEquipoService mantenimientoEquipoService,
                                     ProtocoloClinicoService protocoloClinicoService,
                                     RespaldoService respaldoService,
                                     DisponibilidadService disponibilidadService) {
        this.turnoPersonalService = turnoPersonalService;
        this.mantenimientoEquipoService = mantenimientoEquipoService;
        this.protocoloClinicoService = protocoloClinicoService;
        this.respaldoService = respaldoService;
        this.disponibilidadService = disponibilidadService;
    }

    // RF-42: turnos del personal técnico.
    @GetMapping("/api/turnos")
    @SoloPersonalBancoSangre
    public List<TurnoPersonalDTO> listarTurnos() {
        return turnoPersonalService.listar();
    }

    @PostMapping("/api/turnos")
    @SoloSupervisionBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoPersonalDTO programarTurno(@Valid @RequestBody CrearTurnoRequest request) {
        return turnoPersonalService.programar(request);
    }

    @DeleteMapping("/api/turnos/{id}")
    @SoloSupervisionBancoSangre
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarTurno(@PathVariable Long id) {
        turnoPersonalService.eliminar(id);
    }

    // RF-47: mantenimiento preventivo y calibración de equipos críticos.
    @GetMapping("/api/mantenimientos")
    @SoloPersonalBancoSangre
    public List<MantenimientoEquipoDTO> listarMantenimientos() {
        return mantenimientoEquipoService.listar();
    }

    @PostMapping("/api/mantenimientos")
    @SoloPersonalBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public MantenimientoEquipoDTO registrarMantenimiento(@Valid @RequestBody RegistrarMantenimientoRequest request,
                                                           Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return mantenimientoEquipoService.registrar(request, usuarioId);
    }

    // RF-49: versionado de protocolos y guías clínicas.
    @GetMapping("/api/protocolos")
    @AccesoClinico
    public List<ProtocoloClinicoDTO> listarProtocolos() {
        return protocoloClinicoService.listarTodos();
    }

    @GetMapping("/api/protocolos/vigentes")
    @AccesoClinico
    public List<ProtocoloClinicoDTO> listarProtocolosVigentes() {
        return protocoloClinicoService.listarVigentes();
    }

    @PostMapping("/api/protocolos")
    @SoloSupervisionBancoSangre
    @ResponseStatus(HttpStatus.CREATED)
    public ProtocoloClinicoDTO publicarProtocolo(@Valid @RequestBody PublicarProtocoloRequest request, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return protocoloClinicoService.publicar(request, usuarioId);
    }

    // RF-34: respaldos automáticos con verificación de integridad.
    @GetMapping("/api/respaldos")
    @SoloAdministrador
    public List<RespaldoBaseDatosDTO> listarRespaldos() {
        return respaldoService.listarHistorial();
    }

    @PostMapping("/api/respaldos/ejecutar")
    @SoloAdministrador
    @ResponseStatus(HttpStatus.CREATED)
    public RespaldoBaseDatosDTO ejecutarRespaldo() {
        return respaldoService.ejecutarRespaldo();
    }

    @GetMapping("/api/respaldos/{id}/verificar")
    @SoloAdministrador
    public VerificacionIntegridadDTO verificarIntegridad(@PathVariable Long id) {
        return respaldoService.verificarIntegridad(id);
    }

    // RF-39: monitoreo de disponibilidad (uptime).
    @GetMapping("/api/disponibilidad")
    @SoloAdministrador
    public DisponibilidadDTO obtenerDisponibilidad() {
        return disponibilidadService.obtenerEstado();
    }
}
