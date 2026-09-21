package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.PruebaCompatibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PruebaCompatibilidadRepository extends JpaRepository<PruebaCompatibilidad, Long> {

    @Query("SELECT p FROM PruebaCompatibilidad p JOIN FETCH p.hemocomponente LEFT JOIN FETCH p.tecnologo " +
            "WHERE p.solicitud.id = :solicitudId ORDER BY p.fecha DESC")
    List<PruebaCompatibilidad> listarPorSolicitud(@Param("solicitudId") Long solicitudId);

    Optional<PruebaCompatibilidad> findTopByHemocomponenteIdAndSolicitudIdOrderByFechaDesc(Long hemocomponenteId, Long solicitudId);
}
