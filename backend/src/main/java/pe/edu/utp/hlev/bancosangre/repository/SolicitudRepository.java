package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @Query("SELECT s FROM Solicitud s JOIN FETCH s.paciente JOIN FETCH s.medico ORDER BY s.id DESC")
    List<Solicitud> listarTodasConDetalle();

    long countByEstado(String estado);

    @Query("SELECT s.prioridad, COUNT(s) FROM Solicitud s GROUP BY s.prioridad")
    List<Object[]> contarPorPrioridad();

    @Query("SELECT s.estado, COUNT(s) FROM Solicitud s WHERE s.fechaSolicitud BETWEEN :desde AND :hasta GROUP BY s.estado")
    List<Object[]> contarPorEstadoEnRango(LocalDateTime desde, LocalDateTime hasta);
}
