package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CitaDonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearCitaRequest;
import pe.edu.utp.hlev.bancosangre.dto.ReprogramarCitaRequest;
import pe.edu.utp.hlev.bancosangre.model.CampanaDonacion;
import pe.edu.utp.hlev.bancosangre.model.CitaDonacion;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.repository.CampanaDonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.CitaDonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;

import java.time.LocalDateTime;
import java.util.List;

// RF-22: programación, reprogramación y recordatorio de citas de donación.
@Service
public class CitaDonacionService {

    private static final List<String> ESTADOS_PENDIENTES = List.of("PROGRAMADA", "REPROGRAMADA", "CONFIRMADA");

    private final CitaDonacionRepository citaDonacionRepository;
    private final DonanteRepository donanteRepository;
    private final CampanaDonacionRepository campanaDonacionRepository;

    public CitaDonacionService(CitaDonacionRepository citaDonacionRepository, DonanteRepository donanteRepository,
                                CampanaDonacionRepository campanaDonacionRepository) {
        this.citaDonacionRepository = citaDonacionRepository;
        this.donanteRepository = donanteRepository;
        this.campanaDonacionRepository = campanaDonacionRepository;
    }

    public List<CitaDonacionDTO> listar() {
        return citaDonacionRepository.findAllByOrderByFechaHoraAsc().stream().map(CitaDonacionDTO::from).toList();
    }

    public List<CitaDonacionDTO> listarPorDonante(Long donanteId) {
        return citaDonacionRepository.findByDonanteIdOrderByFechaHoraDesc(donanteId).stream()
                .map(CitaDonacionDTO::from).toList();
    }

    /** RF-22: recordatorio automático — citas pendientes dentro de las próximas `horas` horas. */
    public List<CitaDonacionDTO> proximas(int horas) {
        LocalDateTime ahora = LocalDateTime.now();
        return citaDonacionRepository
                .findByFechaHoraBetweenAndEstadoInOrderByFechaHoraAsc(ahora, ahora.plusHours(horas), ESTADOS_PENDIENTES)
                .stream().map(CitaDonacionDTO::from).toList();
    }

    @Transactional
    public CitaDonacionDTO crear(CrearCitaRequest request) {
        Donante donante = donanteRepository.findById(request.donanteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donante no encontrado."));

        CitaDonacion cita = new CitaDonacion();
        cita.setDonante(donante);
        cita.setFechaHora(request.fechaHora());
        cita.setNotas(request.notas());
        cita.setEstado("PROGRAMADA");

        if (request.campanaId() != null) {
            CampanaDonacion campana = campanaDonacionRepository.findById(request.campanaId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Campaña no encontrada."));
            cita.setCampana(campana);
        }

        return CitaDonacionDTO.from(citaDonacionRepository.save(cita));
    }

    @Transactional
    public CitaDonacionDTO reprogramar(Long id, ReprogramarCitaRequest request) {
        CitaDonacion cita = citaDonacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada."));

        if (request.fechaHora() != null) {
            cita.setFechaHora(request.fechaHora());
            cita.setEstado("REPROGRAMADA");
        } else {
            cita.setEstado(request.estado());
        }
        if (request.notas() != null) {
            cita.setNotas(request.notas());
        }
        cita.setActualizadoEn(LocalDateTime.now());

        return CitaDonacionDTO.from(citaDonacionRepository.save(cita));
    }
}
