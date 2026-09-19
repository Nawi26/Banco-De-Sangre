package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;

import java.time.LocalDateTime;

public record HemocomponenteDTO(
        Long id,
        String codigoProductoIsbt,
        String dinMatriz,
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        Integer volumenMl,
        LocalDateTime fechaVencimiento,
        String ubicacionFisica,
        String estado
) {
    public static HemocomponenteDTO from(Hemocomponente h) {
        return new HemocomponenteDTO(
                h.getId(),
                h.getCodigoProductoIsbt(),
                h.getDonacion() != null ? h.getDonacion().getDinIsbt128() : null,
                h.getTipoHemocomponente(),
                h.getGrupoAbo(),
                h.getFactorRh(),
                h.getVolumenMl(),
                h.getFechaVencimiento(),
                h.getUbicacionFisica(),
                h.getEstado()
        );
    }
}
