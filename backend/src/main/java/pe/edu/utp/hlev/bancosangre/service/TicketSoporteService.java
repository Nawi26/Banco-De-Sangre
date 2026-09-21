package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CrearTicketSoporteRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResponderTicketSoporteRequest;
import pe.edu.utp.hlev.bancosangre.dto.TicketSoporteDTO;
import pe.edu.utp.hlev.bancosangre.model.TicketSoporte;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.TicketSoporteRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** RF-33: mesa de ayuda interna para el registro y seguimiento de incidencias técnicas. */
@Service
public class TicketSoporteService {

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");
    private static final Set<String> ESTADOS = Set.of("ABIERTO", "EN_PROCESO", "RESUELTO", "CERRADO");

    private final TicketSoporteRepository ticketSoporteRepository;
    private final UsuarioRepository usuarioRepository;

    public TicketSoporteService(TicketSoporteRepository ticketSoporteRepository, UsuarioRepository usuarioRepository) {
        this.ticketSoporteRepository = ticketSoporteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<TicketSoporteDTO> listarTodos() {
        return ticketSoporteRepository.listarTodosConDetalle().stream().map(TicketSoporteDTO::from).toList();
    }

    public List<TicketSoporteDTO> listarPropios(Long usuarioId) {
        return ticketSoporteRepository.listarPorUsuario(usuarioId).stream().map(TicketSoporteDTO::from).toList();
    }

    @Transactional
    public TicketSoporteDTO crear(CrearTicketSoporteRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        String prioridad = validar(request.prioridad(), PRIORIDADES, "prioridad");

        TicketSoporte ticket = new TicketSoporte();
        ticket.setUsuario(usuario);
        ticket.setAsunto(request.asunto());
        ticket.setDescripcion(request.descripcion());
        ticket.setPrioridad(prioridad);
        ticket.setEstado("ABIERTO");
        ticketSoporteRepository.save(ticket);

        return TicketSoporteDTO.from(ticket);
    }

    @Transactional
    public TicketSoporteDTO responder(Long id, ResponderTicketSoporteRequest request, Long usuarioId) {
        TicketSoporte ticket = ticketSoporteRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket no encontrado."));
        Usuario responsable = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        String estado = validar(request.estado(), ESTADOS, "estado");

        ticket.setEstado(estado);
        ticket.setRespuesta(request.respuesta());
        ticket.setResueltoPor(responsable);
        ticket.setActualizadoEn(LocalDateTime.now());
        ticketSoporteRepository.save(ticket);

        return TicketSoporteDTO.from(ticket);
    }

    private String validar(String valor, Set<String> validos, String etiqueta) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!validos.contains(normalizado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Valor de " + etiqueta + " no reconocido: " + valor);
        }
        return normalizado;
    }
}
