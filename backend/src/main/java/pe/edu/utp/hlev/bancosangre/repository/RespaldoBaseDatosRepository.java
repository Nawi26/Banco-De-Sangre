package pe.edu.utp.hlev.bancosangre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hlev.bancosangre.model.RespaldoBaseDatos;

import java.util.List;

public interface RespaldoBaseDatosRepository extends JpaRepository<RespaldoBaseDatos, Long> {

    List<RespaldoBaseDatos> findAllByOrderByFechaInicioDesc();
}
