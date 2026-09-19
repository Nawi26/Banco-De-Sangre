package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.CitaDonacion;

import java.time.LocalDateTime;

public record CitaDonacionDTO(
        Long id,
        Long donanteId,
        String donanteNombreCompleto,
        Long campanaId,
        LocalDateTime fechaHora,
        String estado,
        String notas
) {
    public static CitaDonacionDTO from(CitaDonacion c) {
        return new CitaDonacionDTO(
                c.getId(),
                c.getDonante().getId(),
                c.getDonante().getNombres() + " " + c.getDonante().getApellidos(),
                c.getCampana() != null ? c.getCampana().getId() : null,
                c.getFechaHora(),
                c.getEstado(),
                c.getNotas()
        );
    }
}
