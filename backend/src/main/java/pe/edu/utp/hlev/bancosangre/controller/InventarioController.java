package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.InventarioItemDTO;
import pe.edu.utp.hlev.bancosangre.dto.RedBusquedaDTO;
import pe.edu.utp.hlev.bancosangre.dto.ResumenExistenciasDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.service.InventarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RBAC (RF-02): visibilidad de inventario/red (RF-19) abierta a todo el personal clínico.
@RestController
@RequestMapping("/api")
@AccesoClinico
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    // Algoritmo FEFO (First Expired, First Out)
    @GetMapping("/inventario")
    public List<InventarioItemDTO> listarInventario() {
        return inventarioService.listarInventarioActivo();
    }

    @GetMapping("/inventario/resumen")
    public List<ResumenExistenciasDTO> resumenExistencias() {
        return inventarioService.resumenPorGrupoSanguineo();
    }

    @GetMapping("/red-interhospitalaria/{codigo}")
    public RedBusquedaDTO buscarEnRed(@PathVariable String codigo) {
        return inventarioService.buscarPorCodigo(codigo);
    }
}
