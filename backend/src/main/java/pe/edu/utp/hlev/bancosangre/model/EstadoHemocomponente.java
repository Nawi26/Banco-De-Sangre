package pe.edu.utp.hlev.bancosangre.model;

import java.util.List;

/**
 * RF-10: estados físico-biológicos estandarizados de una unidad de hemocomponente.
 * Se centralizan aquí para que todos los módulos (fraccionamiento, tamizaje,
 * inventario, despacho, descarte) usen exactamente el mismo vocabulario.
 */
public final class EstadoHemocomponente {

    public static final String CUARENTENA = "CUARENTENA";
    public static final String DISPONIBLE = "DISPONIBLE";
    // RF-12: reservada tras una prueba cruzada compatible, pendiente de despacho hacia una solicitud.
    public static final String RESERVADO = "RESERVADO";
    public static final String BLOQUEADO = "BLOQUEADO";
    public static final String VENCIDO = "VENCIDO";
    public static final String DESPACHADO = "DESPACHADO";
    public static final String INCINERADO = "INCINERADO";

    // Estados en los que la unidad cuenta como existencia en el inventario activo (FEFO).
    // Incluye "FRACCIONADO" por compatibilidad con datos ya existentes de la versión anterior.
    public static final List<String> ESTADOS_INVENTARIO_ACTIVO = List.of(DISPONIBLE, RESERVADO, "FRACCIONADO");

    private EstadoHemocomponente() {
    }
}
