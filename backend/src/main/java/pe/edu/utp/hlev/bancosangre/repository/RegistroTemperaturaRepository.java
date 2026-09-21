package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.hlev.bancosangre.model.RegistroTemperatura;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroTemperaturaRepository extends JpaRepository<RegistroTemperatura, Long> {

    List<RegistroTemperatura> findByCamaraIdOrderByRegistradoEnDesc(Long camaraId);

    // RF-14/RF-24: alertas recientes por desviación de temperatura fuera de rango.
    @Query("SELECT r FROM RegistroTemperatura r JOIN FETCH r.camara WHERE r.dentroDeRango = false AND r.registradoEn >= :desde ORDER BY r.registradoEn DESC")
    List<RegistroTemperatura> buscarAlertasDesde(LocalDateTime desde);
}
