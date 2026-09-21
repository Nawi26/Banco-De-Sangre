package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record PruebaCompatibilidadDTO(
        Long id,
        Long solicitudId,
        String codigoProductoIsbt,
        String resultadoRai,
        String resultadoPruebaCruzada,
        String tecnologoNombreCompleto,
        LocalDateTime fecha
) {
    public static PruebaCompatibilidadDTO from(pe.edu.utp.hlev.bancosangre.model.PruebaCompatibilidad p) {
        return new PruebaCompatibilidadDTO(
                p.getId(),
                p.getSolicitud().getId(),
                p.getHemocomponente().getCodigoProductoIsbt(),
                p.getResultadoRai(),
                p.getResultadoPruebaCruzada(),
                p.getTecnologo() != null ? p.getTecnologo().getNombres() + " " + p.getTecnologo().getApellidos() : null,
                p.getFecha()
        );
    }
}
