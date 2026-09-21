package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.EventoAdversoDonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarEventoAdversoDonacionRequest;
import pe.edu.utp.hlev.bancosangre.model.Donacion;
import pe.edu.utp.hlev.bancosangre.model.EventoAdversoDonacion;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.EventoAdversoDonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;
import java.util.Set;

/** RF-43: hemovigilancia de eventos adversos ocurridos durante o después de la donación. */
@Service
public class EventoAdversoDonacionService {

    private static final Set<String> TIPOS_EVENTO = Set.of("SINCOPE", "HEMATOMA", "MAREO", "OTRO");
    private static final Set<String> GRAVEDADES = Set.of("LEVE", "MODERADA", "GRAVE");

    private final EventoAdversoDonacionRepository eventoAdversoDonacionRepository;
    private final DonacionRepository donacionRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoAdversoDonacionService(EventoAdversoDonacionRepository eventoAdversoDonacionRepository,
                                         DonacionRepository donacionRepository, UsuarioRepository usuarioRepository) {
        this.eventoAdversoDonacionRepository = eventoAdversoDonacionRepository;
        this.donacionRepository = donacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<EventoAdversoDonacionDTO> listarPorDonacion(Long donacionId) {
        return eventoAdversoDonacionRepository.listarPorDonacion(donacionId).stream()
                .map(EventoAdversoDonacionDTO::from).toList();
    }

    public List<EventoAdversoDonacionDTO> listarTodos() {
        return eventoAdversoDonacionRepository.listarTodosConDetalle().stream()
                .map(EventoAdversoDonacionDTO::from).toList();
    }

    @Transactional
    public EventoAdversoDonacionDTO registrar(Long donacionId, RegistrarEventoAdversoDonacionRequest request, Long usuarioId) {
        Donacion donacion = donacionRepository.findById(donacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donación no encontrada."));

        String tipoEvento = validar(request.tipoEvento(), TIPOS_EVENTO, "tipo de evento");
        String gravedad = validar(request.gravedad(), GRAVEDADES, "gravedad");

        Usuario usuario = usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null;

        EventoAdversoDonacion evento = new EventoAdversoDonacion();
        evento.setDonacion(donacion);
        evento.setTipoEvento(tipoEvento);
        evento.setGravedad(gravedad);
        evento.setDescripcion(request.descripcion());
        evento.setAccionesTomadas(request.accionesTomadas());
        evento.setUsuario(usuario);
        eventoAdversoDonacionRepository.save(evento);

        return EventoAdversoDonacionDTO.from(evento);
    }

    private String validar(String valor, Set<String> validos, String etiqueta) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!validos.contains(normalizado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Valor de " + etiqueta + " no reconocido: " + valor);
        }
        return normalizado;
    }
}
