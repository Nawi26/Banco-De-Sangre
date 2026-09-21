package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    boolean existsByHemocomponenteIdAndEstado(Long hemocomponenteId, String estado);

    @Query("SELECT d FROM Despacho d JOIN FETCH d.solicitud JOIN FETCH d.hemocomponente " +
            "JOIN FETCH d.primeraVerificacionUsuario LEFT JOIN FETCH d.segundaVerificacionUsuario WHERE d.id = :id")
    Optional<Despacho> buscarPorIdConDetalle(@Param("id") Long id);

    @Query("SELECT d FROM Despacho d JOIN FETCH d.solicitud JOIN FETCH d.hemocomponente " +
            "JOIN FETCH d.primeraVerificacionUsuario LEFT JOIN FETCH d.segundaVerificacionUsuario " +
            "WHERE d.estado = :estado ORDER BY d.primeraVerificacionEn ASC")
    List<Despacho> listarPorEstado(@Param("estado") String estado);
}
