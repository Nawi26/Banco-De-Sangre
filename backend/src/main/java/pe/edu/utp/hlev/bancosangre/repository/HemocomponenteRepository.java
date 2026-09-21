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

    List<Hemocomponente> findByDonacionId(Long donacionId);

    // RF-25: candidatas disponibles para una reserva quirúrgica, priorizando la de vencimiento más próximo (FEFO).
    List<Hemocomponente> findByEstadoAndTipoHemocomponenteAndGrupoAboAndFactorRhOrderByFechaVencimientoAsc(
            String estado, String tipoHemocomponente, String grupoAbo, String factorRh);

    List<Hemocomponente> findByReservaQuirurgicaId(Long reservaQuirurgicaId);

    // RF-06: trazabilidad de los hemocomponentes obtenidos de una misma donación (DIN matriz).
    @Query("SELECT h FROM Hemocomponente h JOIN FETCH h.donacion d JOIN FETCH d.donante LEFT JOIN FETCH h.camara WHERE h.donacion.id = :donacionId ORDER BY h.id")
    List<Hemocomponente> findByDonacionIdConDonacion(@Param("donacionId") Long donacionId);

    @Query("SELECT h FROM Hemocomponente h LEFT JOIN FETCH h.donacion LEFT JOIN FETCH h.camara WHERE h.id = :id")
    Optional<Hemocomponente> buscarPorIdConDonacion(@Param("id") Long id);

    // RF-30: historial completo — incluye el donante de la donación de origen.
    @Query("SELECT h FROM Hemocomponente h LEFT JOIN FETCH h.donacion d LEFT JOIN FETCH d.donante LEFT JOIN FETCH h.camara WHERE h.id = :id")
    Optional<Hemocomponente> buscarPorIdParaHistorial(@Param("id") Long id);

    @Query("SELECT h FROM Hemocomponente h LEFT JOIN FETCH h.donacion LEFT JOIN FETCH h.camara WHERE h.codigoProductoIsbt = :codigo")
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
