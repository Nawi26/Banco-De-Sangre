package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Donante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonanteRepository extends JpaRepository<Donante, Long> {
    List<Donante> findAllByOrderByIdDesc();
    boolean existsByNumDoc(String numDoc);
}
