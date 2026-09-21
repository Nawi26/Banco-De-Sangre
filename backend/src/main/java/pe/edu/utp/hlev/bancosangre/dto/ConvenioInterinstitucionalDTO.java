package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record ConvenioInterinstitucionalDTO(
        Long id,
        Long ipressId,
        String ipressNombre,
        String numeroConvenio,
        String objeto,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String estado
) {
    public static ConvenioInterinstitucionalDTO from(pe.edu.utp.hlev.bancosangre.model.ConvenioInterinstitucional c) {
        return new ConvenioInterinstitucionalDTO(
                c.getId(), c.getIpress().getId(), c.getIpress().getNombreEstablecimiento(), c.getNumeroConvenio(),
                c.getObjeto(), c.getFechaInicio(), c.getFechaFin(), c.getEstado()
        );
    }
}
