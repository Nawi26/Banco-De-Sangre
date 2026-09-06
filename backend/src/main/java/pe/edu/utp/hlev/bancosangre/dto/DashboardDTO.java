package pe.edu.utp.hlev.bancosangre.dto;

import java.util.List;
import java.util.Map;

public record DashboardDTO(
        long unidadesDisponibles,
        List<ResumenExistenciasDTO> resumenPorGrupo,
        long totalDonantes,
        long donacionesUltimos30Dias,
        long solicitudesPendientes,
        Map<String, Long> solicitudesPorPrioridad,
        long transfusionesRealizadas,
        long transfusionesConReaccionAdversa,
        long unidadesPorVencerEn7Dias,
        long intercambiosActivos
) {
}
