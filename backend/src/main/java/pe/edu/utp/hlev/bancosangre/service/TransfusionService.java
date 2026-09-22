package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearTransfusionRequest;
import pe.edu.utp.hlev.bancosangre.dto.TransfusionDTO;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.PruebaCompatibilidad;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Transfusion;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.PruebaCompatibilidadRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.TransfusionRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;
import pe.edu.utp.hlev.bancosangre.security.Roles;
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
    private final PruebaCompatibilidadRepository pruebaCompatibilidadRepository;

    public TransfusionService(
            TransfusionRepository transfusionRepository,
            SolicitudRepository solicitudRepository,
            HemocomponenteRepository hemocomponenteRepository,
            UsuarioRepository usuarioRepository,
            PruebaCompatibilidadRepository pruebaCompatibilidadRepository
    ) {
        this.transfusionRepository = transfusionRepository;
        this.solicitudRepository = solicitudRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pruebaCompatibilidadRepository = pruebaCompatibilidadRepository;
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

    // Registra la administración efectiva de una unidad ya despachada (RF-13), descontándola
    // del inventario activo y cerrando la solicitud clínica.
    @Transactional
    public TransfusionDTO crear(CrearTransfusionRequest request, Long usuarioId, String rol) {
        Solicitud solicitud = solicitudRepository.findById(request.solicitudId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        Hemocomponente hemocomponente = hemocomponenteRepository.findByCodigoProductoIsbt(request.codigoProductoIsbt())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe un hemocomponente con ese código ISBT."));

        // RF-12/RF-13: sólo puede transfundirse una unidad que ya completó la prueba cruzada
        // compatible y el despacho con doble verificación electrónica.
        if (!EstadoHemocomponente.DESPACHADO.equals(hemocomponente.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La unidad debe completar la prueba cruzada y el despacho con doble verificación antes de transfundirse.");
        }

        Usuario usuarioActual = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        // El resultado de compatibilidad se toma de la prueba cruzada ya realizada (RF-12),
        // nunca de un valor de entrada del usuario que registra la transfusión.
        String resultadoPruebaCruzada = pruebaCompatibilidadRepository
                .findTopByHemocomponenteIdAndSolicitudIdOrderByFechaDesc(hemocomponente.getId(), solicitud.getId())
                .map(PruebaCompatibilidad::getResultadoPruebaCruzada)
                .orElse("COMPATIBLE");

        Transfusion transfusion = new Transfusion();
        transfusion.setSolicitud(solicitud);
        transfusion.setHemocomponente(hemocomponente);
        transfusion.setPaciente(solicitud.getPaciente());
        transfusion.setResultadoPruebaCruzada(resultadoPruebaCruzada);
        transfusion.setFechaTransfusion(LocalDateTime.now());
        transfusion.setReaccionAdversa(Boolean.TRUE.equals(request.reaccionAdversa()));
        transfusion.setDetallesReaccion(request.detallesReaccion());

        if (Roles.TECNOLOGO_MEDICO.equals(rol) || Roles.JEFE_BANCO_SANGRE.equals(rol)) {
            transfusion.setTecnologo(usuarioActual);
        } else if (Roles.MEDICO_SOLICITANTE.equals(rol)) {
            transfusion.setMedico(usuarioActual);
        }

        Transfusion guardada = transfusionRepository.save(transfusion);

        hemocomponente.setEstado(EstadoHemocomponente.TRANSFUNDIDO);
        hemocomponenteRepository.save(hemocomponente);

        solicitud.setEstado("ATENDIDA");
        solicitudRepository.save(solicitud);

        return TransfusionDTO.from(guardada);
    }
}
