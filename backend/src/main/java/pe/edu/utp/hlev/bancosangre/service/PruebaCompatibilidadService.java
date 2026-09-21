package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.PruebaCompatibilidadDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarPruebaCompatibilidadRequest;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.PruebaCompatibilidad;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.PruebaCompatibilidadRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;
import java.util.Set;

/**
 * RF-12: prueba cruzada y rastreo de anticuerpos irregulares (RAI) de una unidad
 * candidata frente a una solicitud transfusional aprobada. Sólo una prueba cruzada
 * COMPATIBLE reserva la unidad para su posterior despacho (RF-13).
 */
@Service
public class PruebaCompatibilidadService {

    private static final Set<String> RESULTADOS_RAI = Set.of("NEGATIVO", "POSITIVO");
    private static final Set<String> RESULTADOS_PRUEBA_CRUZADA = Set.of("COMPATIBLE", "INCOMPATIBLE");

    private final PruebaCompatibilidadRepository pruebaCompatibilidadRepository;
    private final SolicitudRepository solicitudRepository;
    private final HemocomponenteRepository hemocomponenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HemocomponenteEventoService hemocomponenteEventoService;

    public PruebaCompatibilidadService(PruebaCompatibilidadRepository pruebaCompatibilidadRepository,
                                        SolicitudRepository solicitudRepository,
                                        HemocomponenteRepository hemocomponenteRepository,
                                        UsuarioRepository usuarioRepository,
                                        HemocomponenteEventoService hemocomponenteEventoService) {
        this.pruebaCompatibilidadRepository = pruebaCompatibilidadRepository;
        this.solicitudRepository = solicitudRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.hemocomponenteEventoService = hemocomponenteEventoService;
    }

    public List<PruebaCompatibilidadDTO> listarPorSolicitud(Long solicitudId) {
        return pruebaCompatibilidadRepository.listarPorSolicitud(solicitudId).stream()
                .map(PruebaCompatibilidadDTO::from).toList();
    }

    @Transactional
    public PruebaCompatibilidadDTO registrar(Long solicitudId, RegistrarPruebaCompatibilidadRequest request, Long usuarioId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        if (!"APROBADA".equals(solicitud.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La solicitud debe estar aprobada por el banco de sangre antes de realizar la prueba cruzada.");
        }

        Hemocomponente hemocomponente = hemocomponenteRepository.buscarPorCodigoConDonacion(request.codigoProductoIsbt())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe un hemocomponente con ese código ISBT."));

        if (!EstadoHemocomponente.DISPONIBLE.equals(hemocomponente.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La unidad no está disponible para pruebas de compatibilidad.");
        }

        String resultadoRai = validarResultado(request.resultadoRai(), RESULTADOS_RAI, "RAI");
        String resultadoPruebaCruzada = validarResultado(request.resultadoPruebaCruzada(), RESULTADOS_PRUEBA_CRUZADA, "de prueba cruzada");

        Usuario tecnologo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        PruebaCompatibilidad prueba = new PruebaCompatibilidad();
        prueba.setSolicitud(solicitud);
        prueba.setHemocomponente(hemocomponente);
        prueba.setResultadoRai(resultadoRai);
        prueba.setResultadoPruebaCruzada(resultadoPruebaCruzada);
        prueba.setTecnologo(tecnologo);
        pruebaCompatibilidadRepository.save(prueba);

        if ("COMPATIBLE".equals(resultadoPruebaCruzada)) {
            hemocomponente.setEstado(EstadoHemocomponente.RESERVADO);
            hemocomponenteRepository.save(hemocomponente);
            hemocomponenteEventoService.registrar(hemocomponente, "PRUEBA_CRUZADA_COMPATIBLE",
                    "Compatible (RAI " + resultadoRai + ") para la solicitud " + solicitud.getCodigoSolicitud() + ".", usuarioId);
        } else {
            hemocomponenteEventoService.registrar(hemocomponente, "PRUEBA_CRUZADA_INCOMPATIBLE",
                    "Incompatible (RAI " + resultadoRai + ") frente a la solicitud " + solicitud.getCodigoSolicitud() + ".", usuarioId);
        }

        return PruebaCompatibilidadDTO.from(prueba);
    }

    private String validarResultado(String valor, Set<String> validos, String etiqueta) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!validos.contains(normalizado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Resultado " + etiqueta + " no reconocido: " + valor);
        }
        return normalizado;
    }
}
