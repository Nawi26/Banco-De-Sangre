package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.EventoAdversoTransfusionalDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarEventoAdversoTransfusionalRequest;
import pe.edu.utp.hlev.bancosangre.model.EventoAdversoTransfusional;
import pe.edu.utp.hlev.bancosangre.model.Transfusion;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.EventoAdversoTransfusionalRepository;
import pe.edu.utp.hlev.bancosangre.repository.TransfusionRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;
import java.util.Set;

/**
 * RF-15: hemovigilancia de eventos/incidentes adversos transfusionales, inmediatos o
 * tardíos, trazables hacia la unidad y —a través de ella (RF-30)— hacia la donación de origen.
 */
@Service
public class EventoAdversoTransfusionalService {

    private static final Set<String> TIPOS_REACCION = Set.of("FEBRIL", "ALERGICA", "HEMOLITICA", "OTRA");
    private static final Set<String> GRAVEDADES = Set.of("LEVE", "MODERADA", "GRAVE");

    private final EventoAdversoTransfusionalRepository eventoAdversoTransfusionalRepository;
    private final TransfusionRepository transfusionRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoAdversoTransfusionalService(EventoAdversoTransfusionalRepository eventoAdversoTransfusionalRepository,
                                              TransfusionRepository transfusionRepository,
                                              UsuarioRepository usuarioRepository) {
        this.eventoAdversoTransfusionalRepository = eventoAdversoTransfusionalRepository;
        this.transfusionRepository = transfusionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<EventoAdversoTransfusionalDTO> listarPorTransfusion(Long transfusionId) {
        return eventoAdversoTransfusionalRepository.listarPorTransfusion(transfusionId).stream()
                .map(EventoAdversoTransfusionalDTO::from).toList();
    }

    public List<EventoAdversoTransfusionalDTO> listarTodos() {
        return eventoAdversoTransfusionalRepository.listarTodosConDetalle().stream()
                .map(EventoAdversoTransfusionalDTO::from).toList();
    }

    @Transactional
    public EventoAdversoTransfusionalDTO registrar(Long transfusionId, RegistrarEventoAdversoTransfusionalRequest request, Long usuarioId) {
        Transfusion transfusion = transfusionRepository.findById(transfusionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Transfusión no encontrada."));

        String tipoReaccion = validar(request.tipoReaccion(), TIPOS_REACCION, "tipo de reacción");
        String gravedad = validar(request.gravedad(), GRAVEDADES, "gravedad");

        Usuario usuario = usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null;

        EventoAdversoTransfusional evento = new EventoAdversoTransfusional();
        evento.setTransfusion(transfusion);
        evento.setTipoReaccion(tipoReaccion);
        evento.setEsInmediata(request.esInmediata());
        evento.setGravedad(gravedad);
        evento.setDescripcion(request.descripcion());
        evento.setAccionesTomadas(request.accionesTomadas());
        evento.setUsuario(usuario);
        eventoAdversoTransfusionalRepository.save(evento);

        // Mantiene sincronizado el indicador legado en Transfusion que ya consume la pantalla de "Transfusiones".
        transfusion.setReaccionAdversa(true);
        transfusion.setDetallesReaccion(request.descripcion());
        transfusionRepository.save(transfusion);

        return EventoAdversoTransfusionalDTO.from(evento);
    }

    private String validar(String valor, Set<String> validos, String etiqueta) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!validos.contains(normalizado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Valor de " + etiqueta + " no reconocido: " + valor);
        }
        return normalizado;
    }
}
