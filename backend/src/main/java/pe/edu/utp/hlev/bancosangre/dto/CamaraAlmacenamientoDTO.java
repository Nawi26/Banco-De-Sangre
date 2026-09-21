package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.CamaraAlmacenamiento;

import java.math.BigDecimal;

public record CamaraAlmacenamientoDTO(
        Long id,
        String nombre,
        String tipo,
        String ubicacion,
        BigDecimal temperaturaMinima,
        BigDecimal temperaturaMaxima,
        Boolean activa
) {
    public static CamaraAlmacenamientoDTO from(CamaraAlmacenamiento c) {
        return new CamaraAlmacenamientoDTO(
                c.getId(), c.getNombre(), c.getTipo(), c.getUbicacion(),
                c.getTemperaturaMinima(), c.getTemperaturaMaxima(), c.getActiva()
        );
    }
}
