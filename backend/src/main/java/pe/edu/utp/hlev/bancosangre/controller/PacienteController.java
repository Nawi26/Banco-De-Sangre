package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.CrearPacienteRequest;
import pe.edu.utp.hlev.bancosangre.dto.PacienteDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// RBAC (RF-02): roles con actividad clínico-asistencial.
@RestController
@RequestMapping("/api/pacientes")
@AccesoClinico
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<PacienteDTO> listar() {
        return pacienteService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDTO crear(@Valid @RequestBody CrearPacienteRequest request) {
        return pacienteService.crear(request);
    }
}
