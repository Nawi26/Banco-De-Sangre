package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.ConsentimientoInformado;

import java.util.Optional;

public interface ConsentimientoInformadoRepository extends JpaRepository<ConsentimientoInformado, Long> {
    Optional<ConsentimientoInformado> findFirstByDonanteIdOrderByCreadoEnDesc(Long donanteId);
    Optional<ConsentimientoInformado> findFirstByDonanteIdAndDonacionIsNullOrderByCreadoEnDesc(Long donanteId);
}
