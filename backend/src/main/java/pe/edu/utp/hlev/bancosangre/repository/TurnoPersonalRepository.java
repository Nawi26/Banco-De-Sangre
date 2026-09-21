package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.hlev.bancosangre.model.TurnoPersonal;

import java.util.List;

public interface TurnoPersonalRepository extends JpaRepository<TurnoPersonal, Long> {

    @Query("SELECT t FROM TurnoPersonal t JOIN FETCH t.usuario ORDER BY t.fechaInicio ASC")
    List<TurnoPersonal> listarTodosConDetalle();
}
