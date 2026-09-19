package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDate;

public record ResultadoTriajeDTO(
        boolean apto,
        String tipoDiferimiento,
        String motivo,
        LocalDate diferidoHasta
) {
}
