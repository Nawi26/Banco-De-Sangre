package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;

public record ReservaQuirurgicaDTO(
        Long id,
        Long pacienteId,
        String pacienteNombreCompleto,
        String medicoSolicitanteNombreCompleto,
        String tipoHemocomponente,
        String grupoAbo,
        String factorRh,
        Integer unidadesSolicitadas,
        LocalDateTime fechaCirugiaProgramada,
        Integer horasValidezPostCirugia,
        String estado,
        LocalDateTime creadoEn
) {
    public static ReservaQuirurgicaDTO from(pe.edu.utp.hlev.bancosangre.model.ReservaQuirurgica r) {
        return new ReservaQuirurgicaDTO(
                r.getId(),
                r.getPaciente().getId(),
                r.getPaciente().getNombres() + " " + r.getPaciente().getApellidos(),
                r.getMedicoSolicitante().getNombres() + " " + r.getMedicoSolicitante().getApellidos(),
                r.getTipoHemocomponente(),
                r.getGrupoAbo(),
                r.getFactorRh(),
                r.getUnidadesSolicitadas(),
                r.getFechaCirugiaProgramada(),
                r.getHorasValidezPostCirugia(),
                r.getEstado(),
                r.getCreadoEn()
        );
    }
}
