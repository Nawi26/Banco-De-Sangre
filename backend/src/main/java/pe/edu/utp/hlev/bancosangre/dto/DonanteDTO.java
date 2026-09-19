package pe.edu.utp.hlev.bancosangre.dto;

import java.math.BigDecimal;
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
        Boolean esOcupacionRiesgo,
        String telefono,
        String direccion,
        BigDecimal pesoKg,
        BigDecimal tallaCm,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer pulso,
        BigDecimal hemoglobina,
        Boolean apto,
        String tipoDiferimiento,
        String motivoDiferimiento,
        LocalDate diferidoHasta,
        LocalDate fechaUltimaDonacion
) {
    public static DonanteDTO from(pe.edu.utp.hlev.bancosangre.model.Donante d) {
        return new DonanteDTO(
                d.getId(), d.getTipoDoc(), d.getNumDoc(), d.getNombres(), d.getApellidos(),
                d.getFechaNacimiento(), d.getSexo(), d.getGrupoAbo(), d.getFactorRh(),
                d.getEstadoDiferido(), d.getEsOcupacionRiesgo(), d.getTelefono(), d.getDireccion(),
                d.getPesoKg(), d.getTallaCm(), d.getPresionSistolica(), d.getPresionDiastolica(),
                d.getPulso(), d.getHemoglobina(), d.getApto(), d.getTipoDiferimiento(),
                d.getMotivoDiferimiento(), d.getDiferidoHasta(), d.getFechaUltimaDonacion()
        );
    }
}
