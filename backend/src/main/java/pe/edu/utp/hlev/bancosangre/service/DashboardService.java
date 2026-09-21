package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.DashboardDTO;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudIntercambioRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.TransfusionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final List<String> ESTADOS_ACTIVOS = EstadoHemocomponente.ESTADOS_INVENTARIO_ACTIVO;
    private static final List<String> ESTADOS_INTERCAMBIO_ACTIVOS = List.of("PENDIENTE", "ACEPTADO", "EN_TRANSITO");

    private final HemocomponenteRepository hemocomponenteRepository;
    private final DonanteRepository donanteRepository;
    private final DonacionRepository donacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final TransfusionRepository transfusionRepository;
    private final SolicitudIntercambioRepository intercambioRepository;

    public DashboardService(
            HemocomponenteRepository hemocomponenteRepository,
            DonanteRepository donanteRepository,
            DonacionRepository donacionRepository,
            SolicitudRepository solicitudRepository,
            TransfusionRepository transfusionRepository,
            SolicitudIntercambioRepository intercambioRepository
    ) {
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.donanteRepository = donanteRepository;
        this.donacionRepository = donacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.transfusionRepository = transfusionRepository;
        this.intercambioRepository = intercambioRepository;
    }

    public DashboardDTO obtenerResumen() {
        var resumenPorGrupo = hemocomponenteRepository.resumenPorGrupoSanguineo(ESTADOS_ACTIVOS);
        long unidadesDisponibles = resumenPorGrupo.stream().mapToLong(r -> r.unidades()).sum();

        Map<String, Long> solicitudesPorPrioridad = new LinkedHashMap<>();
        for (Object[] fila : solicitudRepository.contarPorPrioridad()) {
            solicitudesPorPrioridad.put((String) fila[0], (Long) fila[1]);
        }

        LocalDateTime ahora = LocalDateTime.now();

        return new DashboardDTO(
                unidadesDisponibles,
                resumenPorGrupo,
                donanteRepository.count(),
                donacionRepository.countByFechaExtraccionAfter(ahora.minusDays(30)),
                solicitudRepository.countByEstado("PENDIENTE"),
                solicitudesPorPrioridad,
                transfusionRepository.count(),
                transfusionRepository.countByReaccionAdversaTrue(),
                hemocomponenteRepository.countByEstadoInAndFechaVencimientoBetween(ESTADOS_ACTIVOS, ahora, ahora.plusDays(7)),
                intercambioRepository.countByEstadoIn(ESTADOS_INTERCAMBIO_ACTIVOS)
        );
    }
}
