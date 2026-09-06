package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Transfusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransfusionRepository extends JpaRepository<Transfusion, Long> {

    @Query("""
            SELECT t FROM Transfusion t
            JOIN FETCH t.paciente
            JOIN FETCH t.hemocomponente
            JOIN FETCH t.solicitud
            LEFT JOIN FETCH t.tecnologo
            LEFT JOIN FETCH t.medico
            ORDER BY t.id DESC
            """)
    List<Transfusion> listarTodasConDetalle();

    long countByReaccionAdversaTrue();

    boolean existsBySolicitudId(Long solicitudId);
}
