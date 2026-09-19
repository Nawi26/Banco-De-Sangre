package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.CuestionarioTriaje;

import java.util.List;

public interface CuestionarioTriajeRepository extends JpaRepository<CuestionarioTriaje, Long> {
    List<CuestionarioTriaje> findByDonanteIdOrderByFechaEvaluacionDesc(Long donanteId);
}
