package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.ImportarOrdenHisSisRequest;
import pe.edu.utp.hlev.bancosangre.dto.SolicitudDTO;
import pe.edu.utp.hlev.bancosangre.model.Paciente;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.PacienteRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;
import pe.edu.utp.hlev.bancosangre.security.Roles;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * RF-32: integración vía API REST con el HIS/SIS hospitalario para importar
 * automáticamente órdenes médicas de solicitud transfusional. Referencia al paciente
 * y al médico por documento de identidad (num_doc/DNI), ya que el sistema externo no
 * conoce nuestras claves primarias. Es una integración de referencia simplificada: en
 * un HIS/SIS real, el paciente y el médico ya existirían y sólo haría falta vincularlos.
 */
@Service
public class HisSisService {

    private static final Pattern PATRON_CIE10 = Pattern.compile("^[A-TV-Z][0-9]{2}(\\.[0-9A-Z]{1,4})?$");

    private final SolicitudRepository solicitudRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public HisSisService(SolicitudRepository solicitudRepository, PacienteRepository pacienteRepository,
                          UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public SolicitudDTO importarOrden(ImportarOrdenHisSisRequest request) {
        // Idempotencia: si el HIS/SIS reintenta el envío de la misma orden, no se duplica.
        var existente = solicitudRepository.findByCodigoOrdenExterna(request.codigoOrdenExterna());
        if (existente.isPresent()) {
            return SolicitudDTO.from(existente.get());
        }

        if (!PATRON_CIE10.matcher(request.diagnosticoCie10().trim().toUpperCase()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El diagnóstico debe tener un formato CIE-10 válido (ej. \"D50\" o \"O99.0\").");
        }

        Usuario medico = usuarioRepository.findByDniOrEmail(request.medicoDni())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No existe un médico registrado con DNI " + request.medicoDni() + "."));
        if (!Roles.MEDICO_SOLICITANTE.equals(medico.getRol().getNombre())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El usuario con DNI " + request.medicoDni() + " no tiene el rol Médico Solicitante.");
        }

        Paciente paciente = pacienteRepository.findByNumDoc(request.pacienteNumDoc())
                .orElseGet(() -> crearPacienteMinimo(request));

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
        solicitud.setCodigoOrdenExterna(request.codigoOrdenExterna());
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        return SolicitudDTO.from(solicitud);
    }

    private Paciente crearPacienteMinimo(ImportarOrdenHisSisRequest request) {
        Paciente paciente = new Paciente();
        paciente.setTipoDoc("DNI");
        paciente.setNumDoc(request.pacienteNumDoc());
        paciente.setNombres(request.pacienteNombres() != null ? request.pacienteNombres() : "(importado de HIS/SIS)");
        paciente.setApellidos(request.pacienteApellidos() != null ? request.pacienteApellidos() : "");
        return pacienteRepository.save(paciente);
    }

    private String generarCodigo() {
        long siguiente = solicitudRepository.count() + 1;
        return "SOL-" + String.format("%05d", siguiente);
    }
}
