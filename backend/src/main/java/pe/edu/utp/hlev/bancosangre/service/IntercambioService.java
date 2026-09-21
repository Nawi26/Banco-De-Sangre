package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.ActualizarEstadoIntercambioRequest;
import pe.edu.utp.hlev.bancosangre.dto.CrearIntercambioRequest;
import pe.edu.utp.hlev.bancosangre.dto.HospitalDTO;
import pe.edu.utp.hlev.bancosangre.dto.IntercambioDTO;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.IpressEstablecimiento;
import pe.edu.utp.hlev.bancosangre.model.SolicitudIntercambio;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.IpressEstablecimientoRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudIntercambioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class IntercambioService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "PENDIENTE", "ACEPTADO", "EN_TRANSITO", "ENTREGADO", "RECHAZADO", "DEVUELTO"
    );

    private final SolicitudIntercambioRepository intercambioRepository;
    private final IpressEstablecimientoRepository ipressRepository;
    private final HemocomponenteRepository hemocomponenteRepository;

    public IntercambioService(
            SolicitudIntercambioRepository intercambioRepository,
            IpressEstablecimientoRepository ipressRepository,
            HemocomponenteRepository hemocomponenteRepository
    ) {
        this.intercambioRepository = intercambioRepository;
        this.ipressRepository = ipressRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
    }

    public List<HospitalDTO> listarHospitales() {
        return ipressRepository.findAllByOrderByNombreEstablecimientoAsc()
                .stream()
                .map(HospitalDTO::from)
                .toList();
    }

    public List<IntercambioDTO> listarIntercambios() {
        return intercambioRepository.listarTodos();
    }

    @Transactional
    public Long crearIntercambio(CrearIntercambioRequest request) {
        if (request.ipressSolicitanteId().equals(request.ipressProveedoraId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El hospital solicitante y el proveedor no pueden ser el mismo.");
        }

        IpressEstablecimiento solicitante = ipressRepository.findById(request.ipressSolicitanteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hospital solicitante no encontrado."));
        IpressEstablecimiento proveedor = ipressRepository.findById(request.ipressProveedoraId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hospital proveedor no encontrado."));
        Hemocomponente hemocomponente = hemocomponenteRepository.findByCodigoProductoIsbt(request.codigoProductoIsbt())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe un hemocomponente con ese código ISBT."));

        SolicitudIntercambio solicitud = new SolicitudIntercambio();
        solicitud.setIpressSolicitante(solicitante);
        solicitud.setIpressProveedora(proveedor);
        solicitud.setHemocomponente(hemocomponente);
        solicitud.setEstado("PENDIENTE");
        solicitud.setResponsableTransporte(request.responsableTransporte());
        solicitud.setFechaSolicitud(LocalDateTime.now());

        return intercambioRepository.save(solicitud).getId();
    }

    @Transactional
    public void actualizarEstado(Long id, ActualizarEstadoIntercambioRequest request) {
        if (!ESTADOS_VALIDOS.contains(request.estado())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado inválido.");
        }

        SolicitudIntercambio solicitud = intercambioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Intercambio no encontrado."));

        solicitud.setEstado(request.estado());
        if (request.temperaturaCadenaFrio() != null) {
            solicitud.setTemperaturaCadenaFrio(request.temperaturaCadenaFrio());
        }
        solicitud.setFechaRespuesta(LocalDateTime.now());

        intercambioRepository.save(solicitud);
    }

    /**
     * RF-40: registra la devolución de una unidad prestada a la red interhospitalaria,
     * actualizando automáticamente el inventario de origen (la unidad vuelve a estar
     * DISPONIBLE en el banco de sangre que la prestó).
     */
    @Transactional
    public void registrarDevolucion(Long id) {
        SolicitudIntercambio solicitud = intercambioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Intercambio no encontrado."));

        if (!"ENTREGADO".equals(solicitud.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Sólo se puede devolver una unidad que ya fue entregada.");
        }

        solicitud.setEstado("DEVUELTO");
        solicitud.setFechaRespuesta(LocalDateTime.now());
        intercambioRepository.save(solicitud);

        Hemocomponente hemocomponente = solicitud.getHemocomponente();
        hemocomponente.setEstado(pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente.DISPONIBLE);
        hemocomponenteRepository.save(hemocomponente);
    }
}
