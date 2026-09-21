package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.hlev.bancosangre.dto.AlertaStockCriticoDTO;
import pe.edu.utp.hlev.bancosangre.dto.InventarioItemDTO;
import pe.edu.utp.hlev.bancosangre.dto.ResumenExistenciasDTO;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.TipoHemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF-14: alertas visuales automáticas ante proximidad de vencimiento de unidades
 * biológicas o quiebre de stock crítico de seguridad (por tipo de hemocomponente y
 * grupo ABO/Rh). Las alertas de temperatura (RF-24) se resuelven aparte en
 * {@link TemperaturaService}, ya que responden a un umbral físico distinto.
 */
@Service
public class AlertaService {

    private static final int DIAS_ALERTA_VENCIMIENTO = 7;
    // Umbral de seguridad simplificado: por debajo de esta cantidad de unidades de un
    // mismo tipo+grupo+Rh se considera quiebre de stock crítico.
    private static final long UMBRAL_STOCK_CRITICO = 5L;
    private static final List<String> GRUPOS_ABO = List.of("O", "A", "B", "AB");
    private static final List<String> FACTORES_RH = List.of("POSITIVO", "NEGATIVO");

    private static final List<String> ESTADOS_ACTIVOS = EstadoHemocomponente.ESTADOS_INVENTARIO_ACTIVO;

    private final HemocomponenteRepository hemocomponenteRepository;

    public AlertaService(HemocomponenteRepository hemocomponenteRepository) {
        this.hemocomponenteRepository = hemocomponenteRepository;
    }

    public List<InventarioItemDTO> unidadesPorVencer() {
        LocalDateTime limite = LocalDateTime.now().plusDays(DIAS_ALERTA_VENCIMIENTO);
        return hemocomponenteRepository.findByEstadoInOrderByFechaVencimientoAsc(ESTADOS_ACTIVOS).stream()
                .filter(h -> h.getFechaVencimiento() != null && h.getFechaVencimiento().isBefore(limite))
                .map(InventarioItemDTO::from)
                .toList();
    }

    /**
     * Recorre TODAS las combinaciones tipo x grupo x Rh posibles (no sólo las que ya
     * tienen unidades en inventario), para que un quiebre total (0 unidades) también
     * se reporte como alerta crítica y no quede invisible por ausencia de filas.
     */
    public List<AlertaStockCriticoDTO> stockCritico() {
        Map<String, Long> existenciasPorCombinacion = new HashMap<>();
        for (ResumenExistenciasDTO resumen : hemocomponenteRepository.resumenPorGrupoSanguineo(ESTADOS_ACTIVOS)) {
            existenciasPorCombinacion.put(clave(resumen.tipoHemocomponente(), resumen.grupoAbo(), resumen.factorRh()), resumen.unidades());
        }

        List<AlertaStockCriticoDTO> alertas = new ArrayList<>();
        for (TipoHemocomponente tipo : TipoHemocomponente.values()) {
            for (String grupo : GRUPOS_ABO) {
                for (String rh : FACTORES_RH) {
                    long unidades = existenciasPorCombinacion.getOrDefault(clave(tipo.getNombre(), grupo, rh), 0L);
                    if (unidades < UMBRAL_STOCK_CRITICO) {
                        alertas.add(new AlertaStockCriticoDTO(tipo.getNombre(), grupo, rh, unidades, UMBRAL_STOCK_CRITICO));
                    }
                }
            }
        }
        return alertas;
    }

    private String clave(String tipo, String grupo, String rh) {
        return tipo + "|" + grupo + "|" + rh;
    }
}
