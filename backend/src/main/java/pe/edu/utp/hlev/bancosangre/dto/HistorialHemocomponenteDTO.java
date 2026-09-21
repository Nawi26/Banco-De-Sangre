package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-30: historial completo del ciclo de vida de una unidad, desde la donación
 * de origen hasta su destino final (despacho, descarte o transferencia).
 */
public record HistorialHemocomponenteDTO(
        HemocomponenteDTO hemocomponente,
        String donanteNombreCompleto,
        LocalDateTime fechaExtraccion,
        String resultadoTamizaje,
        DescarteHemocomponenteDTO descarte,
        List<HemocomponenteEventoDTO> eventos
) {
}
