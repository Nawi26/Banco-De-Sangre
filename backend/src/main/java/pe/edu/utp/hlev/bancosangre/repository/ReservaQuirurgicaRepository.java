package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.ReservaQuirurgica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservaQuirurgicaRepository extends JpaRepository<ReservaQuirurgica, Long> {

    @Query("SELECT r FROM ReservaQuirurgica r JOIN FETCH r.paciente JOIN FETCH r.medicoSolicitante ORDER BY r.fechaCirugiaProgramada ASC")
    List<ReservaQuirurgica> listarConDetalle();

    List<ReservaQuirurgica> findByEstado(String estado);

    @Query("SELECT r FROM ReservaQuirurgica r JOIN FETCH r.paciente JOIN FETCH r.medicoSolicitante WHERE r.id = :id")
    Optional<ReservaQuirurgica> buscarPorIdConDetalle(@Param("id") Long id);
}
