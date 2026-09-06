package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDate;

public record PacienteDTO(
        Long id,
        String tipoDoc,
        String numDoc,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String sexo,
        String grupoAbo,
        String factorRh
) {
    public static PacienteDTO from(pe.edu.utp.hlev.bancosangre.model.Paciente p) {
        return new PacienteDTO(
                p.getId(), p.getTipoDoc(), p.getNumDoc(), p.getNombres(), p.getApellidos(),
                p.getFechaNacimiento(), p.getSexo(), p.getGrupoAbo(), p.getFactorRh()
        );
    }
}
