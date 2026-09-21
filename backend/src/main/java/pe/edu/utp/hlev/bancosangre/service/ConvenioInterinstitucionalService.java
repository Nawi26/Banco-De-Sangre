package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.ConvenioInterinstitucionalDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearConvenioRequest;
import pe.edu.utp.hlev.bancosangre.model.ConvenioInterinstitucional;
import pe.edu.utp.hlev.bancosangre.model.IpressEstablecimiento;
import pe.edu.utp.hlev.bancosangre.repository.ConvenioInterinstitucionalRepository;
import pe.edu.utp.hlev.bancosangre.repository.IpressEstablecimientoRepository;

import java.util.List;
import java.util.Set;

/** RF-45: convenios y contratos interinstitucionales vigentes para el intercambio de hemocomponentes. */
@Service
public class ConvenioInterinstitucionalService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("VIGENTE", "VENCIDO", "RESCINDIDO");

    private final ConvenioInterinstitucionalRepository convenioInterinstitucionalRepository;
    private final IpressEstablecimientoRepository ipressEstablecimientoRepository;

    public ConvenioInterinstitucionalService(ConvenioInterinstitucionalRepository convenioInterinstitucionalRepository,
                                              IpressEstablecimientoRepository ipressEstablecimientoRepository) {
        this.convenioInterinstitucionalRepository = convenioInterinstitucionalRepository;
        this.ipressEstablecimientoRepository = ipressEstablecimientoRepository;
    }

    public List<ConvenioInterinstitucionalDTO> listar() {
        return convenioInterinstitucionalRepository.listarTodosConDetalle().stream()
                .map(ConvenioInterinstitucionalDTO::from).toList();
    }

    @Transactional
    public ConvenioInterinstitucionalDTO crear(CrearConvenioRequest request) {
        IpressEstablecimiento ipress = ipressEstablecimientoRepository.findById(request.ipressId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Establecimiento no encontrado."));

        ConvenioInterinstitucional convenio = new ConvenioInterinstitucional();
        convenio.setIpress(ipress);
        convenio.setNumeroConvenio(request.numeroConvenio());
        convenio.setObjeto(request.objeto());
        convenio.setFechaInicio(request.fechaInicio());
        convenio.setFechaFin(request.fechaFin());
        convenio.setEstado("VIGENTE");
        convenioInterinstitucionalRepository.save(convenio);

        return ConvenioInterinstitucionalDTO.from(convenio);
    }

    @Transactional
    public void actualizarEstado(Long id, String estado) {
        String normalizado = estado == null ? "" : estado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(normalizado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estado);
        }

        ConvenioInterinstitucional convenio = convenioInterinstitucionalRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Convenio no encontrado."));
        convenio.setEstado(normalizado);
        convenioInterinstitucionalRepository.save(convenio);
    }
}
