package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.HemocomponenteEvento;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteEventoRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

// RF-30: registra cada hito del ciclo de vida de una unidad para su trazabilidad completa.
@Service
public class HemocomponenteEventoService {

    private final HemocomponenteEventoRepository hemocomponenteEventoRepository;
    private final UsuarioRepository usuarioRepository;

    public HemocomponenteEventoService(HemocomponenteEventoRepository hemocomponenteEventoRepository,
                                        UsuarioRepository usuarioRepository) {
        this.hemocomponenteEventoRepository = hemocomponenteEventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void registrar(Hemocomponente hemocomponente, String tipoEvento, String detalle, Long usuarioId) {
        HemocomponenteEvento evento = new HemocomponenteEvento();
        evento.setHemocomponente(hemocomponente);
        evento.setTipoEvento(tipoEvento);
        evento.setDetalle(detalle);
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(evento::setUsuario);
        }
        hemocomponenteEventoRepository.save(evento);
    }
}
