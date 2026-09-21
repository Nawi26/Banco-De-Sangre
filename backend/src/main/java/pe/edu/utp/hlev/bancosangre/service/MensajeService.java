package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.EnviarMensajeRequest;
import pe.edu.utp.hlev.bancosangre.dto.MensajeDTO;
import pe.edu.utp.hlev.bancosangre.model.Mensaje;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.MensajeRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;

/**
 * RF-48: canal de mensajería interna entre el personal del banco de sangre y el
 * servicio asistencial solicitante, como hilo de coordinación asociado a la solicitud.
 */
@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public MensajeService(MensajeRepository mensajeRepository, SolicitudRepository solicitudRepository,
                           UsuarioRepository usuarioRepository) {
        this.mensajeRepository = mensajeRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MensajeDTO> listarPorSolicitud(Long solicitudId) {
        return mensajeRepository.listarPorSolicitud(solicitudId).stream().map(MensajeDTO::from).toList();
    }

    @Transactional
    public MensajeDTO enviar(Long solicitudId, EnviarMensajeRequest request, Long usuarioId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));
        Usuario remitente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        Mensaje mensaje = new Mensaje();
        mensaje.setSolicitud(solicitud);
        mensaje.setRemitente(remitente);
        mensaje.setContenido(request.contenido());
        mensajeRepository.save(mensaje);

        return MensajeDTO.from(mensaje);
    }
}
