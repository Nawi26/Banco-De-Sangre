package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.hlev.bancosangre.model.MantenimientoEquipo;

import java.util.List;

public interface MantenimientoEquipoRepository extends JpaRepository<MantenimientoEquipo, Long> {

    @Query("SELECT m FROM MantenimientoEquipo m LEFT JOIN FETCH m.responsable ORDER BY m.fechaRealizado DESC")
    List<MantenimientoEquipo> listarTodosConDetalle();
}
