package pe.edu.utp.hlev.bancosangre.dto;

public record AlertaStockCriticoDTO(
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        long unidadesDisponibles,
        long umbralCritico
) {
}
