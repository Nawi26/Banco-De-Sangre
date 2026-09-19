package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.CampanaDonacion;

import java.time.LocalDate;

public record CampanaDonacionDTO(
        Long id,
        String nombre,
        String institucion,
        String tipo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer metaUnidades,
        Integer unidadesLogradas,
        String estado
) {
    public static CampanaDonacionDTO from(CampanaDonacion c) {
        return new CampanaDonacionDTO(
                c.getId(), c.getNombre(), c.getInstitucion(), c.getTipo(),
                c.getFechaInicio(), c.getFechaFin(), c.getMetaUnidades(),
                c.getUnidadesLogradas(), c.getEstado()
        );
    }
}
