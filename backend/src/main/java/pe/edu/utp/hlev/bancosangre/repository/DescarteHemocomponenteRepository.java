package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.DescarteHemocomponente;

import java.util.Optional;

public interface DescarteHemocomponenteRepository extends JpaRepository<DescarteHemocomponente, Long> {
    Optional<DescarteHemocomponente> findByHemocomponenteId(Long hemocomponenteId);
}
