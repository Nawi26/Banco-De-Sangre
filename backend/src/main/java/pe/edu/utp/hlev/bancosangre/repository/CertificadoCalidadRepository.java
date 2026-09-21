package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hlev.bancosangre.model.CertificadoCalidad;

import java.util.Optional;

public interface CertificadoCalidadRepository extends JpaRepository<CertificadoCalidad, Long> {

    Optional<CertificadoCalidad> findByHemocomponenteId(Long hemocomponenteId);

    @Query("SELECT c FROM CertificadoCalidad c JOIN FETCH c.hemocomponente h LEFT JOIN FETCH h.donacion WHERE c.codigoVerificacion = :codigo")
    Optional<CertificadoCalidad> buscarPorCodigoVerificacion(@Param("codigo") String codigo);
}
