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
import java.util.regex.Pattern;

@Service
public class SolicitudService {

    // RF-11: DESPACHADA la fija DespachoService al completarse la doble verificación (RF-13).
    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "APROBADA", "DESPACHADA", "ATENDIDA", "RECHAZADA");

    // Formato estándar de un código CIE-10: letra + 2 dígitos, con subcategoría decimal opcional (ej. "D50", "O99.0").
    private static final Pattern PATRON_CIE10 = Pattern.compile("^[A-TV-Z][0-9]{2}(\\.[0-9A-Z]{1,4})?$");

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

        if (!PATRON_CIE10.matcher(request.diagnosticoCie10().trim().toUpperCase()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El diagnóstico debe tener un formato CIE-10 válido (ej. \"D50\" o \"O99.0\").");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setCodigoSolicitud(generarCodigo());
        solicitud.setPaciente(paciente);
        solicitud.setMedico(medico);
        solicitud.setTipoHemocomponente(request.tipoHemocomponente());
        solicitud.setUnidadesSolicitadas(request.unidadesSolicitadas());
        solicitud.setPrioridad(request.prioridad());
        solicitud.setEstado("PENDIENTE");
        solicitud.setIndicacionClinica(request.indicacionClinica());
        solicitud.setDiagnosticoCie10(request.diagnosticoCie10().trim().toUpperCase());
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
