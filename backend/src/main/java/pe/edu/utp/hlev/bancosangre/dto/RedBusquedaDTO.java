package pe.edu.utp.hlev.bancosangre.dto;

public record RedBusquedaDTO(
        String codigoProductoIsbt,
        String grupoAbo,
        String factorRh,
        String estado,
        String ubicacionFisica
) {
    public static RedBusquedaDTO from(pe.edu.utp.hlev.bancosangre.model.Hemocomponente h) {
        return new RedBusquedaDTO(
                h.getCodigoProductoIsbt(),
                h.getGrupoAbo(),
                h.getFactorRh(),
                h.getEstado(),
                h.getUbicacionFisica()
        );
    }
}
