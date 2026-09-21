package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findAllByOrderByIdDesc();
    boolean existsByNumDoc(String numDoc);
    Optional<Paciente> findByNumDoc(String numDoc);
}
