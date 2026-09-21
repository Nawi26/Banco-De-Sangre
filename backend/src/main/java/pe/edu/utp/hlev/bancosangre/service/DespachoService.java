package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.ConfirmarDespachoRequest;
import pe.edu.utp.hlev.bancosangre.dto.DespachoDTO;
import pe.edu.utp.hlev.bancosangre.dto.IniciarDespachoRequest;
import pe.edu.utp.hlev.bancosangre.model.Despacho;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.DespachoRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-13: doble verificación electrónica del despacho. Dos responsables distintos,
 * cada uno con su propia firma electrónica (RF-31), deben confirmar antes de que
 * la unidad —ya reservada por una prueba cruzada compatible (RF-12)— salga del
 * banco de sangre hacia la solicitud transfusional.
 */
@Service
public class DespachoService {

    private static final String ESTADO_PENDIENTE_SEGUNDA_VERIFICACION = "PENDIENTE_SEGUNDA_VERIFICACION";
    private static final String ESTADO_DESPACHADO = "DESPACHADO";

    private final DespachoRepository despachoRepository;
    private final SolicitudRepository solicitudRepository;
    private final HemocomponenteRepository hemocomponenteRepository;
    private final FirmaElectronicaService firmaElectronicaService;
    private final HemocomponenteEventoService hemocomponenteEventoService;

    public DespachoService(DespachoRepository despachoRepository, SolicitudRepository solicitudRepository,
                            HemocomponenteRepository hemocomponenteRepository,
                            FirmaElectronicaService firmaElectronicaService,
                            HemocomponenteEventoService hemocomponenteEventoService) {
        this.despachoRepository = despachoRepository;
        this.solicitudRepository = solicitudRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.firmaElectronicaService = firmaElectronicaService;
        this.hemocomponenteEventoService = hemocomponenteEventoService;
    }

    public List<DespachoDTO> listarPendientes() {
        return despachoRepository.listarPorEstado(ESTADO_PENDIENTE_SEGUNDA_VERIFICACION).stream()
                .map(DespachoDTO::from).toList();
    }

    /** Primera verificación: quien prepara la unidad para el despacho. */
    @Transactional
    public DespachoDTO iniciar(IniciarDespachoRequest request, Long usuarioId) {
        Hemocomponente hemocomponente = hemocomponenteRepository.buscarPorCodigoConDonacion(request.codigoProductoIsbt())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe un hemocomponente con ese código ISBT."));

        if (!EstadoHemocomponente.RESERVADO.equals(hemocomponente.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La unidad debe tener una prueba cruzada compatible antes de iniciar el despacho.");
        }
        if (despachoRepository.existsByHemocomponenteIdAndEstado(hemocomponente.getId(), ESTADO_PENDIENTE_SEGUNDA_VERIFICACION)) {
            throw new ApiException(HttpStatus.CONFLICT, "Esta unidad ya tiene un despacho pendiente de segunda verificación.");
        }

        Solicitud solicitud = solicitudRepository.findById(request.solicitudId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada."));

        Usuario primerVerificador = firmaElectronicaService.validar(usuarioId, request.claveConfirmacion());

        Despacho despacho = new Despacho();
        despacho.setSolicitud(solicitud);
        despacho.setHemocomponente(hemocomponente);
        despacho.setPrimeraVerificacionUsuario(primerVerificador);
        despacho.setPrimeraVerificacionEn(LocalDateTime.now());
        despacho.setEstado(ESTADO_PENDIENTE_SEGUNDA_VERIFICACION);
        despachoRepository.save(despacho);

        hemocomponenteEventoService.registrar(hemocomponente, "DESPACHO_PRIMERA_VERIFICACION",
                "Primera verificación para el despacho hacia la solicitud " + solicitud.getCodigoSolicitud() + ".", usuarioId);

        return DespachoDTO.from(despacho);
    }

    /** Segunda verificación: un responsable distinto confirma y libera el despacho. */
    @Transactional
    public DespachoDTO confirmar(Long despachoId, ConfirmarDespachoRequest request, Long usuarioId) {
        Despacho despacho = despachoRepository.buscarPorIdConDetalle(despachoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Despacho no encontrado."));

        if (!ESTADO_PENDIENTE_SEGUNDA_VERIFICACION.equals(despacho.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Este despacho no está pendiente de segunda verificación.");
        }

        Usuario segundoVerificador = firmaElectronicaService.validar(usuarioId, request.claveConfirmacion());
        if (despacho.getPrimeraVerificacionUsuario().getId().equals(segundoVerificador.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La segunda verificación del despacho debe ser realizada por un responsable distinto al de la primera.");
        }

        despacho.setSegundaVerificacionUsuario(segundoVerificador);
        despacho.setSegundaVerificacionEn(LocalDateTime.now());
        despacho.setEstado(ESTADO_DESPACHADO);
        despacho.setFechaDespacho(LocalDateTime.now());
        despachoRepository.save(despacho);

        Hemocomponente hemocomponente = despacho.getHemocomponente();
        hemocomponente.setEstado(EstadoHemocomponente.DESPACHADO);
        hemocomponenteRepository.save(hemocomponente);

        Solicitud solicitud = despacho.getSolicitud();
        solicitud.setEstado("DESPACHADA");
        solicitudRepository.save(solicitud);

        hemocomponenteEventoService.registrar(hemocomponente, "DESPACHO",
                "Despacho confirmado con doble verificación electrónica para la solicitud " + solicitud.getCodigoSolicitud() + ".", usuarioId);

        return DespachoDTO.from(despacho);
    }
}
