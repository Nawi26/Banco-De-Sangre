package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.TamizajeSerologico;

import java.time.LocalDateTime;
import java.util.List;

public record TamizajeSerologicoDTO(
        Long id,
        Long donacionId,
        String origen,
        String estado,
        String resultadoGeneral,
        LocalDateTime fechaPrimeraDigitacion,
        LocalDateTime fechaSegundaDigitacion,
        List<ResultadoMarcadorDTO> resultados
) {
    public static TamizajeSerologicoDTO from(TamizajeSerologico t) {
        return new TamizajeSerologicoDTO(
                t.getId(),
                t.getDonacion().getId(),
                t.getOrigen(),
                t.getEstado(),
                t.getResultadoGeneral(),
                t.getFechaPrimeraDigitacion(),
                t.getFechaSegundaDigitacion(),
                t.getResultados().stream().map(ResultadoMarcadorDTO::from).toList()
        );
    }
}
