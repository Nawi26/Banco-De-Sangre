package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearTransfusionRequest;
import pe.edu.utp.hlev.bancosangre.dto.TransfusionDTO;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Transfusion;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.TransfusionRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransfusionService {

    private final TransfusionRepository transfusionRepository;
    private final SolicitudRepository solicitudRepository;
    private final HemocomponenteRepository hemocomponenteRepository;
    private final UsuarioRepository usuarioRepository;

    public TransfusionService(
            TransfusionRepository transfusionRepository,
            SolicitudRepository solicitudRepository,
            HemocomponenteRepository hemocomponenteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.transfusionRepository = transfusionRepository;
        this.solicitudRepository = solicitudRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<TransfusionDTO> listar() {
        return transfusionRepository.listarTodasConDetalle().stream().map(TransfusionDTO::from).toList();
    }

    public long contarTotal() {
        return transfusionRepository.count();
    }

    public long contarConReaccionAdversa() {
        return transfusionRepository.countByReaccionAdversaTrue();
    }

    // Registra la transfusión, descuenta la unidad del inventario activo y cierra la solicitud clínica
    @Transactional
    public TransfusionDTO crear(CrearTransfusionRequest request, Long usuarioId, String rol) {
        Solicitud solicitud = solicitudRepository.findById(request.solicitudId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        Hemocomponente hemocomponente = hemocomponenteRepository.findByCodigoProductoIsbt(request.codigoProductoIsbt())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe un hemocomponente con ese código ISBT."));

        if (!"DISPONIBLE".equals(hemocomponente.getEstado()) && !"FRACCIONADO".equals(hemocomponente.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La unidad seleccionada no está disponible para transfusión.");
        }

        Usuario usuarioActual = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        Transfusion transfusion = new Transfusion();
        transfusion.setSolicitud(solicitud);
        transfusion.setHemocomponente(hemocomponente);
        transfusion.setPaciente(solicitud.getPaciente());
        transfusion.setResultadoPruebaCruzada(request.resultadoPruebaCruzada());
        transfusion.setFechaTransfusion(LocalDateTime.now());
        transfusion.setReaccionAdversa(Boolean.TRUE.equals(request.reaccionAdversa()));
        transfusion.setDetallesReaccion(request.detallesReaccion());

        if ("Tecnologo_Medico".equals(rol)) {
            transfusion.setTecnologo(usuarioActual);
        } else if ("Medico_Tratante".equals(rol)) {
            transfusion.setMedico(usuarioActual);
        }

        Transfusion guardada = transfusionRepository.save(transfusion);

        hemocomponente.setEstado("TRANSFUNDIDO");
        hemocomponenteRepository.save(hemocomponente);

        solicitud.setEstado("ATENDIDA");
        solicitudRepository.save(solicitud);

        return TransfusionDTO.from(guardada);
    }
}
