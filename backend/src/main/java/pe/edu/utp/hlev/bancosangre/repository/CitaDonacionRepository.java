package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.CitaDonacion;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaDonacionRepository extends JpaRepository<CitaDonacion, Long> {
    List<CitaDonacion> findAllByOrderByFechaHoraAsc();
    List<CitaDonacion> findByDonanteIdOrderByFechaHoraDesc(Long donanteId);

    // RF-22: soporte de recordatorio automático (citas próximas, aún no atendidas).
    List<CitaDonacion> findByFechaHoraBetweenAndEstadoInOrderByFechaHoraAsc(
            LocalDateTime desde, LocalDateTime hasta, List<String> estados);
}
