package pe.edu.utp.hlev.bancosangre.dto;

public record ResumenExistenciasDTO(
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        Long unidades,
        Long volumenTotalMl
) {
}
