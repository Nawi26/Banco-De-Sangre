package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.hlev.bancosangre.model.ConvenioInterinstitucional;

import java.util.List;

public interface ConvenioInterinstitucionalRepository extends JpaRepository<ConvenioInterinstitucional, Long> {

    @Query("SELECT c FROM ConvenioInterinstitucional c JOIN FETCH c.ipress ORDER BY c.fechaInicio DESC")
    List<ConvenioInterinstitucional> listarTodosConDetalle();
}
