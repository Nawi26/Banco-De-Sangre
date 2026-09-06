package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record InventarioItemDTO(
        String codigoProductoIsbt,
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        LocalDateTime fechaVencimiento,
        String estado
) {
    public static InventarioItemDTO from(pe.edu.utp.hlev.bancosangre.model.Hemocomponente h) {
        return new InventarioItemDTO(
                h.getCodigoProductoIsbt(),
                h.getTipoHemocomponente(),
                h.getGrupoAbo(),
                h.getFactorRh(),
                h.getFechaVencimiento(),
                h.getEstado()
        );
    }
}
