package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hlev.bancosangre.model.HemocomponenteEvento;

import java.time.LocalDateTime;
import java.util.List;

public interface HemocomponenteEventoRepository extends JpaRepository<HemocomponenteEvento, Long> {

    @Query("SELECT e FROM HemocomponenteEvento e LEFT JOIN FETCH e.usuario WHERE e.hemocomponente.id = :hemocomponenteId ORDER BY e.fecha ASC")
    List<HemocomponenteEvento> findByHemocomponenteIdOrderByFechaAsc(@Param("hemocomponenteId") Long hemocomponenteId);

    long countByTipoEventoAndFechaBetween(String tipoEvento, LocalDateTime desde, LocalDateTime hasta);
}
