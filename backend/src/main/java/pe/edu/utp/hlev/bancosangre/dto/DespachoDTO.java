package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record DespachoDTO(
        Long id,
        String codigoSolicitud,
        String codigoProductoIsbt,
        String primeraVerificacionNombreCompleto,
        LocalDateTime primeraVerificacionEn,
        String segundaVerificacionNombreCompleto,
        LocalDateTime segundaVerificacionEn,
        String estado,
        LocalDateTime fechaDespacho
) {
    public static DespachoDTO from(pe.edu.utp.hlev.bancosangre.model.Despacho d) {
        return new DespachoDTO(
                d.getId(),
                d.getSolicitud().getCodigoSolicitud(),
                d.getHemocomponente().getCodigoProductoIsbt(),
                d.getPrimeraVerificacionUsuario().getNombres() + " " + d.getPrimeraVerificacionUsuario().getApellidos(),
                d.getPrimeraVerificacionEn(),
                d.getSegundaVerificacionUsuario() != null
                        ? d.getSegundaVerificacionUsuario().getNombres() + " " + d.getSegundaVerificacionUsuario().getApellidos()
                        : null,
                d.getSegundaVerificacionEn(),
                d.getEstado(),
                d.getFechaDespacho()
        );
    }
}
