package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record DonacionDTO(
        Long id,
        String dinIsbt128,
        Long donanteId,
        String donanteNombreCompleto,
        LocalDateTime fechaExtraccion,
        Integer volumenMl,
        String tipoDonacion,
        Boolean tamizajeAprobado
) {
    public static DonacionDTO from(pe.edu.utp.hlev.bancosangre.model.Donacion d) {
        return new DonacionDTO(
                d.getId(),
                d.getDinIsbt128(),
                d.getDonante().getId(),
                d.getDonante().getNombres() + " " + d.getDonante().getApellidos(),
                d.getFechaExtraccion(),
                d.getVolumenMl(),
                d.getTipoDonacion(),
                d.getTamizajeAprobado()
        );
    }
}
