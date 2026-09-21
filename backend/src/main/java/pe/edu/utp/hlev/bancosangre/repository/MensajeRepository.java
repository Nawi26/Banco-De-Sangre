package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m JOIN FETCH m.remitente r JOIN FETCH r.rol " +
            "WHERE m.solicitud.id = :solicitudId ORDER BY m.fechaEnvio ASC")
    List<Mensaje> listarPorSolicitud(@Param("solicitudId") Long solicitudId);
}
