package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.RegistroTemperatura;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistroTemperaturaDTO(
        Long id,
        Long camaraId,
        String camaraNombre,
        BigDecimal temperatura,
        Boolean dentroDeRango,
        LocalDateTime registradoEn
) {
    public static RegistroTemperaturaDTO from(RegistroTemperatura r) {
        return new RegistroTemperaturaDTO(
                r.getId(), r.getCamara().getId(), r.getCamara().getNombre(),
                r.getTemperatura(), r.getDentroDeRango(), r.getRegistradoEn()
        );
    }
}
