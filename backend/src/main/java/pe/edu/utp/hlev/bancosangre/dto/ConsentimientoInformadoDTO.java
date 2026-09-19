package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.ConsentimientoInformado;

import java.time.LocalDateTime;

public record ConsentimientoInformadoDTO(
        Long id,
        Long donanteId,
        String tipoValidacion,
        Boolean aceptado,
        LocalDateTime creadoEn
) {
    public static ConsentimientoInformadoDTO from(ConsentimientoInformado c) {
        return new ConsentimientoInformadoDTO(
                c.getId(), c.getDonante().getId(), c.getTipoValidacion(), c.getAceptado(), c.getCreadoEn()
        );
    }
}
