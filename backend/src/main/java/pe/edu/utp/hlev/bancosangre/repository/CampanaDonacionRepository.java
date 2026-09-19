package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.CampanaDonacion;

import java.util.List;

public interface CampanaDonacionRepository extends JpaRepository<CampanaDonacion, Long> {
    List<CampanaDonacion> findAllByOrderByFechaInicioDesc();
}
