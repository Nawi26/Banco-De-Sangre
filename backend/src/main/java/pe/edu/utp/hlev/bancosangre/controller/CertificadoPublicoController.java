package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.VerificacionCertificadoDTO;
import pe.edu.utp.hlev.bancosangre.service.CertificadoCalidadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RF-41: verificación pública de autenticidad de un certificado de calidad
 * (destinada a ser escaneada por cualquiera desde el QR, sin iniciar sesión).
 * Ver SecurityConfig: esta ruta está explícitamente exceptuada del filtro de autenticación.
 */
@RestController
@RequestMapping("/api/certificados")
public class CertificadoPublicoController {

    private final CertificadoCalidadService certificadoCalidadService;

    public CertificadoPublicoController(CertificadoCalidadService certificadoCalidadService) {
        this.certificadoCalidadService = certificadoCalidadService;
    }

    @GetMapping("/verificar/{codigo}")
    public VerificacionCertificadoDTO verificar(@PathVariable String codigo) {
        return certificadoCalidadService.verificar(codigo);
    }
}
