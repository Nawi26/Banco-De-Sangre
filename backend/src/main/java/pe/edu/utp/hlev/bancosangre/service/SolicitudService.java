package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.ActualizarEstadoSolicitudRequest;
import pe.edu.utp.hlev.bancosangre.dto.CrearSolicitudRequest;
import pe.edu.utp.hlev.bancosangre.dto.SolicitudDTO;
import pe.edu.utp.hlev.bancosangre.model.Paciente;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.PacienteRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.TransfusionRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class SolicitudService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "APROBADA", "ATENDIDA", "RECHAZADA");

    private final SolicitudRepository solicitudRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransfusionRepository transfusionRepository;

    public SolicitudService(SolicitudRepository solicitudRepository, PacienteRepository pacienteRepository,
                             UsuarioRepository usuarioRepository, TransfusionRepository transfusionRepository) {
        this.solicitudRepository = solicitudRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.transfusionRepository = transfusionRepository;
    }

    public List<SolicitudDTO> listar() {
        return solicitudRepository.listarTodasConDetalle().stream().map(SolicitudDTO::from).toList();
    }

    public long contarPendientes() {
        return solicitudRepository.countByEstado("PENDIENTE");
    }

    @Transactional
    public SolicitudDTO crear(CrearSolicitudRequest request, Long medicoId) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado."));
        Usuario medico = usuarioRepository.findById(medicoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Médico no encontrado."));

        Solicitud solicitud = new Solicitud();
        solicitud.setCodigoSolicitud(generarCodigo());
        solicitud.setPaciente(paciente);
        solicitud.setMedico(medico);
        solicitud.setTipoHemocomponente(request.tipoHemocomponente());
        solicitud.setUnidadesSolicitadas(request.unidadesSolicitadas());
        solicitud.setPrioridad(request.prioridad());
        solicitud.setEstado("PENDIENTE");
        solicitud.setIndicacionClinica(request.indicacionClinica());
        solicitud.setFechaSolicitud(LocalDateTime.now());

        return SolicitudDTO.from(solicitudRepository.save(solicitud));
    }

    @Transactional
    public void actualizarEstado(Long id, ActualizarEstadoSolicitudRequest request) {
        if (!ESTADOS_VALIDOS.contains(request.estado())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado inválido.");
        }

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        solicitud.setEstado(request.estado());
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void eliminar(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        if (transfusionRepository.existsBySolicitudId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede eliminar: la solicitud ya tiene una transfusión registrada.");
        }

        solicitudRepository.delete(solicitud);
    }

    private String generarCodigo() {
        long siguiente = solicitudRepository.count() + 1;
        return "SOL-" + String.format("%05d", siguiente);
    }
}
