package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.model.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    @Query("SELECT d FROM Donacion d JOIN FETCH d.donante ORDER BY d.id DESC")
    List<Donacion> listarTodasConDonante();

    long countByFechaExtraccionAfter(LocalDateTime desde);
}
