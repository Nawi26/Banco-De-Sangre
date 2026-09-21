package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.AlertaStockCriticoDTO;
import pe.edu.utp.hlev.bancosangre.dto.InventarioItemDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.service.AlertaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// RF-14: alertas visuales de proximidad de vencimiento y quiebre de stock crítico.
@RestController
@RequestMapping("/api/alertas")
@AccesoClinico
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/vencimiento")
    public List<InventarioItemDTO> unidadesPorVencer() {
        return alertaService.unidadesPorVencer();
    }

    @GetMapping("/stock-critico")
    public List<AlertaStockCriticoDTO> stockCritico() {
        return alertaService.stockCritico();
    }
}
