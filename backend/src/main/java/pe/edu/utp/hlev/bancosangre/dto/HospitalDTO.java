package pe.edu.utp.hlev.bancosangre.dto;

public record HospitalDTO(
        Long id,
        String codigoRenipress,
        String nombreEstablecimiento,
        String categoriaIpress,
        String tipoBancoSangre
) {
    public static HospitalDTO from(pe.edu.utp.hlev.bancosangre.model.IpressEstablecimiento h) {
        return new HospitalDTO(
                h.getId(),
                h.getCodigoRenipress(),
                h.getNombreEstablecimiento(),
                h.getCategoriaIpress(),
                h.getTipoBancoSangre()
        );
    }
}
