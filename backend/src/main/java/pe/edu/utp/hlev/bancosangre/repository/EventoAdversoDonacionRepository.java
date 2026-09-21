package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.EventoAdversoDonacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventoAdversoDonacionRepository extends JpaRepository<EventoAdversoDonacion, Long> {

    @Query("SELECT e FROM EventoAdversoDonacion e LEFT JOIN FETCH e.usuario " +
            "WHERE e.donacion.id = :donacionId ORDER BY e.fechaDeteccion DESC")
    List<EventoAdversoDonacion> listarPorDonacion(@Param("donacionId") Long donacionId);

    @Query("SELECT e FROM EventoAdversoDonacion e JOIN FETCH e.donacion d JOIN FETCH d.donante " +
            "LEFT JOIN FETCH e.usuario ORDER BY e.fechaDeteccion DESC")
    List<EventoAdversoDonacion> listarTodosConDetalle();
}
