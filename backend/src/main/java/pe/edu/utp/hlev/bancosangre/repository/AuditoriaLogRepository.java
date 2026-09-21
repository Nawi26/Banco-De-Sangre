package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pe.edu.utp.hlev.bancosangre.model.AuditoriaLog;

/**
 * Repositorio de sólo lectura/inserción para la bitácora de auditoría (RF-17, RNF-04).
 * Extiende {@link Repository} (no CrudRepository) a propósito: no expone
 * ningún método update/delete, de forma que el registro sea inmutable a nivel de código.
 */
public interface AuditoriaLogRepository extends Repository<AuditoriaLog, Long> {

    AuditoriaLog save(AuditoriaLog auditoriaLog);

    Page<AuditoriaLog> findAllByOrderByCreadoEnDesc(Pageable pageable);

    Page<AuditoriaLog> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId, Pageable pageable);

    // RF-44: cantidad total de acciones críticas auditadas, como señal de cumplimiento (RNF-04).
    @Query("SELECT COUNT(a) FROM AuditoriaLog a")
    long contarTodos();
}
