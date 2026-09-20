package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hlev.bancosangre.model.TamizajeSerologico;

import java.util.Optional;

public interface TamizajeSerologicoRepository extends JpaRepository<TamizajeSerologico, Long> {

    boolean existsByDonacionId(Long donacionId);

    @Query("SELECT t FROM TamizajeSerologico t JOIN FETCH t.donacion LEFT JOIN FETCH t.resultados WHERE t.donacion.id = :donacionId")
    Optional<TamizajeSerologico> buscarPorDonacionConResultados(@Param("donacionId") Long donacionId);
}
