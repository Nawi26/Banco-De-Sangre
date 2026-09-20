package pe.edu.utp.hlev.bancosangre.dto;

import pe.edu.utp.hlev.bancosangre.model.ResultadoMarcador;

public record ResultadoMarcadorDTO(
        String marcador,
        String resultadoDigitacion1,
        String resultadoDigitacion2,
        String resultadoFinal,
        Boolean concordante
) {
    public static ResultadoMarcadorDTO from(ResultadoMarcador r) {
        return new ResultadoMarcadorDTO(
                r.getMarcador(), r.getResultadoDigitacion1(), r.getResultadoDigitacion2(),
                r.getResultadoFinal(), r.getConcordante()
        );
    }
}
