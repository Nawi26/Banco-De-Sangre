package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.EventoDisponibilidad;

import java.util.List;
import java.util.Optional;

public interface EventoDisponibilidadRepository extends JpaRepository<EventoDisponibilidad, Long> {

    List<EventoDisponibilidad> findAllByOrderByFechaDesc();

    Optional<EventoDisponibilidad> findFirstByTipoOrderByFechaDesc(String tipo);
}
