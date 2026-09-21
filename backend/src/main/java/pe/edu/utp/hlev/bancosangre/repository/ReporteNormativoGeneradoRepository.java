package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.ReporteNormativoGenerado;

import java.util.List;

public interface ReporteNormativoGeneradoRepository extends JpaRepository<ReporteNormativoGenerado, Long> {

    List<ReporteNormativoGenerado> findAllByOrderByFechaGeneracionDesc();
}
