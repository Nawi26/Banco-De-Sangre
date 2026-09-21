package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.DescartarHemocomponenteRequest;
import pe.edu.utp.hlev.bancosangre.dto.DescarteHemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.model.DescarteHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.DescarteHemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;

import java.util.List;
import java.util.Set;

// RF-29: descarte de una unidad (caducidad, rotura, contaminación) con evidencia fotográfica.
@Service
public class DescarteService {

    private static final Set<String> ESTADOS_TERMINALES = Set.of(EstadoHemocomponente.DESPACHADO, EstadoHemocomponente.INCINERADO);
    private static final List<String> MOTIVOS_VALIDOS = List.of("CADUCIDAD", "ROTURA", "CONTAMINACION", "OTRO");

    private final HemocomponenteRepository hemocomponenteRepository;
    private final DescarteHemocomponenteRepository descarteHemocomponenteRepository;
    private final FirmaElectronicaService firmaElectronicaService;
    private final HemocomponenteEventoService hemocomponenteEventoService;

    public DescarteService(HemocomponenteRepository hemocomponenteRepository,
                            DescarteHemocomponenteRepository descarteHemocomponenteRepository,
                            FirmaElectronicaService firmaElectronicaService,
                            HemocomponenteEventoService hemocomponenteEventoService) {
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.descarteHemocomponenteRepository = descarteHemocomponenteRepository;
        this.firmaElectronicaService = firmaElectronicaService;
        this.hemocomponenteEventoService = hemocomponenteEventoService;
    }

    @Transactional
    public DescarteHemocomponenteDTO descartar(Long hemocomponenteId, DescartarHemocomponenteRequest request, Long usuarioId) {
        Hemocomponente hemocomponente = hemocomponenteRepository.findById(hemocomponenteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hemocomponente no encontrado."));

        if (ESTADOS_TERMINALES.contains(hemocomponente.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La unidad ya se encuentra en un estado final (" + hemocomponente.getEstado() + ") y no puede descartarse.");
        }
        if (!MOTIVOS_VALIDOS.contains(request.motivo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Motivo de descarte no reconocido: " + request.motivo());
        }

        var usuario = firmaElectronicaService.validar(usuarioId, request.claveConfirmacion());

        hemocomponente.setEstado(EstadoHemocomponente.INCINERADO);
        hemocomponenteRepository.save(hemocomponente);

        DescarteHemocomponente descarte = new DescarteHemocomponente();
        descarte.setHemocomponente(hemocomponente);
        descarte.setMotivo(request.motivo());
        descarte.setEvidenciaFoto(request.evidenciaFoto());
        descarte.setUsuario(usuario);

        DescarteHemocomponente guardado = descarteHemocomponenteRepository.save(descarte);
        hemocomponenteEventoService.registrar(hemocomponente, "DESCARTE",
                "Motivo: " + request.motivo() + ".", usuarioId);

        return DescarteHemocomponenteDTO.from(guardado);
    }
}
