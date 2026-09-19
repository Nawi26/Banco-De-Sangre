package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonacionDTO;
import pe.edu.utp.hlev.bancosangre.model.CampanaDonacion;
import pe.edu.utp.hlev.bancosangre.model.Donacion;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.repository.CampanaDonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final DonanteRepository donanteRepository;
    private final CampanaDonacionRepository campanaDonacionRepository;
    private final ConsentimientoService consentimientoService;
    private final CampanaDonacionService campanaDonacionService;

    public DonacionService(DonacionRepository donacionRepository, DonanteRepository donanteRepository,
                            CampanaDonacionRepository campanaDonacionRepository,
                            ConsentimientoService consentimientoService,
                            CampanaDonacionService campanaDonacionService) {
        this.donacionRepository = donacionRepository;
        this.donanteRepository = donanteRepository;
        this.campanaDonacionRepository = campanaDonacionRepository;
        this.consentimientoService = consentimientoService;
        this.campanaDonacionService = campanaDonacionService;
    }

    public List<DonacionDTO> listar() {
        return donacionRepository.listarTodasConDonante().stream().map(DonacionDTO::from).toList();
    }

    public long contarDesde(LocalDateTime desde) {
        return donacionRepository.countByFechaExtraccionAfter(desde);
    }

    @Transactional
    public DonacionDTO crear(CrearDonacionRequest request) {
        Donante donante = donanteRepository.findById(request.donanteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donante no encontrado."));

        // RF-04: bloquea la extracción si el donante está diferido (temporal o permanente).
        if (!Boolean.TRUE.equals(donante.getApto())) {
            String motivo = donante.getMotivoDiferimiento() != null ? donante.getMotivoDiferimiento() : "diferimiento vigente";
            String hasta = donante.getDiferidoHasta() != null ? " hasta " + donante.getDiferidoHasta() : "";
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "El donante está diferido (" + donante.getTipoDiferimiento() + "): " + motivo + hasta + ".");
        }

        // RF-38: exige el consentimiento informado vigente antes de la extracción.
        if (!consentimientoService.tieneConsentimientoVigente(donante.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Debe registrarse el consentimiento informado del donante antes de la extracción.");
        }

        Donacion donacion = new Donacion();
        donacion.setDinIsbt128(generarDinIsbt128());
        donacion.setDonante(donante);
        donacion.setFechaExtraccion(LocalDateTime.now());
        donacion.setVolumenMl(request.volumenMl());
        donacion.setTipoDonacion(request.tipoDonacion() != null && !request.tipoDonacion().isBlank() ? request.tipoDonacion() : "VOLUNTARIA");
        donacion.setTamizajeAprobado(false);

        if (request.campanaId() != null) {
            CampanaDonacion campana = campanaDonacionRepository.findById(request.campanaId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Campaña no encontrada."));
            donacion.setCampana(campana);
        }

        Donacion guardada = donacionRepository.save(donacion);

        consentimientoService.vincularADonacion(donante.getId(), guardada);

        donante.setFechaUltimaDonacion(LocalDate.now());
        donanteRepository.save(donante);

        if (guardada.getCampana() != null) {
            campanaDonacionService.incrementarUnidadesLogradas(guardada.getCampana());
        }

        // Recargar con el donante ya inicializado para armar el DTO sin problemas de lazy-loading
        return DonacionDTO.from(guardada);
    }

    private String generarDinIsbt128() {
        long siguiente = donacionRepository.count() + 1;
        return "W" + String.format("%011d", siguiente);
    }
}
