package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.EventoAdversoTransfusional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoAdversoTransfusionalRepository extends JpaRepository<EventoAdversoTransfusional, Long> {

    long countByFechaDeteccionBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT e FROM EventoAdversoTransfusional e JOIN FETCH e.transfusion t JOIN FETCH t.paciente " +
            "LEFT JOIN FETCH t.hemocomponente LEFT JOIN FETCH e.usuario " +
            "WHERE e.transfusion.id = :transfusionId ORDER BY e.fechaDeteccion DESC")
    List<EventoAdversoTransfusional> listarPorTransfusion(@Param("transfusionId") Long transfusionId);

    @Query("SELECT e FROM EventoAdversoTransfusional e JOIN FETCH e.transfusion t JOIN FETCH t.paciente " +
            "LEFT JOIN FETCH t.hemocomponente LEFT JOIN FETCH e.usuario ORDER BY e.fechaDeteccion DESC")
    List<EventoAdversoTransfusional> listarTodosConDetalle();
}
