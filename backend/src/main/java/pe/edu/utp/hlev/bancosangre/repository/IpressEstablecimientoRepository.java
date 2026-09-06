package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.IpressEstablecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IpressEstablecimientoRepository extends JpaRepository<IpressEstablecimiento, Long> {
    List<IpressEstablecimiento> findAllByOrderByNombreEstablecimientoAsc();
}
