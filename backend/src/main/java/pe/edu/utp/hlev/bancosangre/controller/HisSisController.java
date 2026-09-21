package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.ImportarOrdenHisSisRequest;
import pe.edu.utp.hlev.bancosangre.dto.SolicitudDTO;
import pe.edu.utp.hlev.bancosangre.security.AccesoClinico;
import pe.edu.utp.hlev.bancosangre.service.HisSisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// RF-32: integración RESTful con el HIS/SIS hospitalario para importar automáticamente
// órdenes médicas de solicitud transfusional.
@RestController
@RequestMapping("/api/his-sis")
@AccesoClinico
public class HisSisController {

    private final HisSisService hisSisService;

    public HisSisController(HisSisService hisSisService) {
        this.hisSisService = hisSisService;
    }

    @PostMapping("/ordenes-transfusionales")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudDTO importarOrden(@Valid @RequestBody ImportarOrdenHisSisRequest request) {
        return hisSisService.importarOrden(request);
    }
}
