package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.hlev.bancosangre.model.ProtocoloClinico;

import java.util.List;

public interface ProtocoloClinicoRepository extends JpaRepository<ProtocoloClinico, Long> {

    @Query("SELECT p FROM ProtocoloClinico p LEFT JOIN FETCH p.publicadoPor ORDER BY p.nombre ASC, p.fechaPublicacion DESC")
    List<ProtocoloClinico> listarTodosConDetalle();

    List<ProtocoloClinico> findByNombreAndVigenteTrue(String nombre);

    @Query("SELECT p FROM ProtocoloClinico p LEFT JOIN FETCH p.publicadoPor WHERE p.vigente = true ORDER BY p.nombre ASC")
    List<ProtocoloClinico> listarVigentes();
}
