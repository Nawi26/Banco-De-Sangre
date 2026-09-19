package pe.edu.utp.hlev.bancosangre.dto;

import java.time.LocalDate;

/**
 * RF-05: contenido normalizado de la etiqueta ISBT 128 de una unidad, lista para
 * codificarse como código de barras lineal (código de producto) y bidimensional
 * (payload con DIN, producto, grupo ABO/Rh y vencimiento). El renderizado gráfico
 * del código de barras/QR se realiza en el cliente a partir de estos datos.
 */
public record IsbtEtiquetaDTO(
        String codigoLineal,
        String payload2D,
        String dinMatriz,
        String codigoProducto,
        String grupoAboRh,
        LocalDate fechaVencimiento
) {
}
