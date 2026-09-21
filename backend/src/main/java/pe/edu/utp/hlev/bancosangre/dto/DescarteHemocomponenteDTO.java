package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.DescarteHemocomponente;

import java.time.LocalDateTime;

public record DescarteHemocomponenteDTO(
        Long id,
        Long hemocomponenteId,
        String motivo,
        boolean tieneEvidenciaFoto,
        LocalDateTime fecha
) {
    public static DescarteHemocomponenteDTO from(DescarteHemocomponente d) {
        return new DescarteHemocomponenteDTO(
                d.getId(), d.getHemocomponente().getId(), d.getMotivo(),
                d.getEvidenciaFoto() != null && !d.getEvidenciaFoto().isBlank(), d.getFecha()
        );
    }
}
