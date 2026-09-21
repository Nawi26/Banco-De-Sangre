package pe.edu.utp.hlev.bancosangre.dto;

// RF-46: proyección de desabastecimiento por tipo de hemocomponente y grupo ABO/Rh.
public record ProyeccionDesabastecimientoDTO(
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        long stockActual,
        double consumoDiarioPromedio,
        Double diasCoberturaEstimados,
        String nivelRiesgo
) {
}
