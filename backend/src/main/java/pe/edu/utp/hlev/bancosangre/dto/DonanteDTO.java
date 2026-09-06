package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDate;

public record DonanteDTO(
        Long id,
        String tipoDoc,
        String numDoc,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String sexo,
        String grupoAbo,
        String factorRh,
        Boolean estadoDiferido,
        Boolean esOcupacionRiesgo
) {
    public static DonanteDTO from(pe.edu.utp.hlev.bancosangre.model.Donante d) {
        return new DonanteDTO(
                d.getId(), d.getTipoDoc(), d.getNumDoc(), d.getNombres(), d.getApellidos(),
                d.getFechaNacimiento(), d.getSexo(), d.getGrupoAbo(), d.getFactorRh(),
                d.getEstadoDiferido(), d.getEsOcupacionRiesgo()
        );
    }
}
