package pe.edu.utp.hlev.bancosangre.repository;

import pe.edu.utp.hlev.bancosangre.dto.IntercambioDTO;
import pe.edu.utp.hlev.bancosangre.model.SolicitudIntercambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SolicitudIntercambioRepository extends JpaRepository<SolicitudIntercambio, Long> {

    @Query("""
            SELECT new pe.edu.utp.hlev.bancosangre.dto.IntercambioDTO(
                si.id, si.estado, si.temperaturaCadenaFrio, si.responsableTransporte,
                si.fechaSolicitud, si.fechaRespuesta,
                h.codigoProductoIsbt, h.tipoHemocomponente, h.grupoAbo, h.factorRh,
                sol.nombreEstablecimiento, prov.nombreEstablecimiento
            )
            FROM SolicitudIntercambio si
            JOIN si.hemocomponente h
            JOIN si.ipressSolicitante sol
            JOIN si.ipressProveedora prov
            ORDER BY si.id DESC
            """)
    List<IntercambioDTO> listarTodos();

    long countByEstadoIn(List<String> estados);
}
