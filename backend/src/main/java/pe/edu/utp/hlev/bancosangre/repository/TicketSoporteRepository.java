package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hlev.bancosangre.model.TicketSoporte;

import java.util.List;

public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {

    @Query("SELECT t FROM TicketSoporte t JOIN FETCH t.usuario LEFT JOIN FETCH t.resueltoPor ORDER BY t.creadoEn DESC")
    List<TicketSoporte> listarTodosConDetalle();

    @Query("SELECT t FROM TicketSoporte t JOIN FETCH t.usuario LEFT JOIN FETCH t.resueltoPor " +
            "WHERE t.usuario.id = :usuarioId ORDER BY t.creadoEn DESC")
    List<TicketSoporte> listarPorUsuario(@Param("usuarioId") Long usuarioId);
}
