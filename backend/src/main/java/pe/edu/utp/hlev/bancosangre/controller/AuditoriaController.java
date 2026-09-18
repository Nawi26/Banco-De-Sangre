package pe.edu.utp.hlev.bancosangre.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.hlev.bancosangre.dto.AuditoriaLogDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloSupervisionBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.AuditoriaService;

// RF-16/RF-17/RF-30: consulta de la bitácora de auditoría inmutable.
@RestController
@RequestMapping("/api/auditoria")
@SoloSupervisionBancoSangre
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public Page<AuditoriaLogDTO> listar(@RequestParam(defaultValue = "0") int pagina,
                                         @RequestParam(defaultValue = "50") int tamano,
                                         @RequestParam(required = false) Long usuarioId) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamano, 200));
        return usuarioId != null
                ? auditoriaService.listarPorUsuario(usuarioId, pageable)
                : auditoriaService.listar(pageable);
    }
}
