package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.CamaraAlmacenamiento;

import java.util.List;

public interface CamaraAlmacenamientoRepository extends JpaRepository<CamaraAlmacenamiento, Long> {
    List<CamaraAlmacenamiento> findAllByOrderByNombreAsc();
}
