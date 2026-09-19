package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.dto.ResumenExistenciasDTO;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HemocomponenteRepository extends JpaRepository<Hemocomponente, Long> {

    List<Hemocomponente> findByEstadoInOrderByFechaVencimientoAsc(List<String> estados);

    Optional<Hemocomponente> findByCodigoProductoIsbt(String codigoProductoIsbt);

    long countByEstadoInAndFechaVencimientoBetween(List<String> estados, LocalDateTime desde, LocalDateTime hasta);

    long countByDonacionIdAndTipoHemocomponente(Long donacionId, String tipoHemocomponente);

    // RF-06: trazabilidad de los hemocomponentes obtenidos de una misma donación (DIN matriz).
    @Query("SELECT h FROM Hemocomponente h JOIN FETCH h.donacion d JOIN FETCH d.donante WHERE h.donacion.id = :donacionId ORDER BY h.id")
    List<Hemocomponente> findByDonacionIdConDonacion(@Param("donacionId") Long donacionId);

    @Query("SELECT h FROM Hemocomponente h LEFT JOIN FETCH h.donacion WHERE h.id = :id")
    Optional<Hemocomponente> buscarPorIdConDonacion(@Param("id") Long id);

    @Query("SELECT h FROM Hemocomponente h LEFT JOIN FETCH h.donacion WHERE h.codigoProductoIsbt = :codigo")
    Optional<Hemocomponente> buscarPorCodigoConDonacion(@Param("codigo") String codigo);

    @Query("""
            SELECT new pe.edu.utp.hlev.bancosangre.dto.ResumenExistenciasDTO(
                h.tipoHemocomponente, h.grupoAbo, h.factorRh, COUNT(h), COALESCE(SUM(h.volumenMl), 0L)
            )
            FROM Hemocomponente h
            WHERE h.estado IN :estados
            GROUP BY h.tipoHemocomponente, h.grupoAbo, h.factorRh
            ORDER BY h.tipoHemocomponente, h.grupoAbo, h.factorRh
            """)
    List<ResumenExistenciasDTO> resumenPorGrupoSanguineo(@Param("estados") List<String> estados);
}
