package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.hlev.bancosangre.dto.ProyeccionDesabastecimientoDTO;
import pe.edu.utp.hlev.bancosangre.dto.ResumenExistenciasDTO;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.TipoHemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.TransfusionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF-46: proyecta escenarios de desabastecimiento comparando la demanda histórica
 * (transfusiones de los últimos {@value #DIAS_HISTORICO} días) frente al stock disponible,
 * por tipo de hemocomponente y grupo ABO/Rh.
 */
@Service
public class ProyeccionDesabastecimientoService {

    private static final int DIAS_HISTORICO = 30;
    private static final double UMBRAL_DIAS_RIESGO_ALTO = 3.0;
    private static final double UMBRAL_DIAS_RIESGO_MEDIO = 7.0;
    private static final List<String> GRUPOS_ABO = List.of("O", "A", "B", "AB");
    private static final List<String> FACTORES_RH = List.of("POSITIVO", "NEGATIVO");
    private static final List<String> ESTADOS_ACTIVOS = EstadoHemocomponente.ESTADOS_INVENTARIO_ACTIVO;

    private final HemocomponenteRepository hemocomponenteRepository;
    private final TransfusionRepository transfusionRepository;

    public ProyeccionDesabastecimientoService(HemocomponenteRepository hemocomponenteRepository,
                                               TransfusionRepository transfusionRepository) {
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.transfusionRepository = transfusionRepository;
    }

    public List<ProyeccionDesabastecimientoDTO> proyectar() {
        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = hasta.minusDays(DIAS_HISTORICO);

        Map<String, Long> consumoPorCombinacion = new HashMap<>();
        for (var t : transfusionRepository.listarEnRangoConHemocomponente(desde, hasta)) {
            var h = t.getHemocomponente();
            String clave = clave(h.getTipoHemocomponente(), h.getGrupoAbo(), h.getFactorRh());
            consumoPorCombinacion.merge(clave, 1L, Long::sum);
        }

        Map<String, Long> stockPorCombinacion = new HashMap<>();
        for (ResumenExistenciasDTO resumen : hemocomponenteRepository.resumenPorGrupoSanguineo(ESTADOS_ACTIVOS)) {
            stockPorCombinacion.put(clave(resumen.tipoHemocomponente(), resumen.grupoAbo(), resumen.factorRh()), resumen.unidades());
        }

        List<ProyeccionDesabastecimientoDTO> proyecciones = new ArrayList<>();
        for (TipoHemocomponente tipo : TipoHemocomponente.values()) {
            for (String grupo : GRUPOS_ABO) {
                for (String rh : FACTORES_RH) {
                    String clave = clave(tipo.getNombre(), grupo, rh);
                    long stockActual = stockPorCombinacion.getOrDefault(clave, 0L);
                    long transfusionesEnPeriodo = consumoPorCombinacion.getOrDefault(clave, 0L);
                    double consumoDiario = transfusionesEnPeriodo / (double) DIAS_HISTORICO;

                    Double diasCobertura = consumoDiario > 0 ? stockActual / consumoDiario : null;
                    String riesgo = calcularRiesgo(stockActual, diasCobertura);

                    proyecciones.add(new ProyeccionDesabastecimientoDTO(
                            tipo.getNombre(), grupo, rh, stockActual, redondear(consumoDiario), diasCobertura != null ? redondear(diasCobertura) : null, riesgo
                    ));
                }
            }
        }
        return proyecciones;
    }

    private String calcularRiesgo(long stockActual, Double diasCobertura) {
        if (stockActual == 0) {
            return "ALTO";
        }
        if (diasCobertura == null) {
            return "BAJO"; // sin consumo histórico registrado, no hay base para proyectar riesgo
        }
        if (diasCobertura < UMBRAL_DIAS_RIESGO_ALTO) {
            return "ALTO";
        }
        if (diasCobertura < UMBRAL_DIAS_RIESGO_MEDIO) {
            return "MEDIO";
        }
        return "BAJO";
    }

    private String clave(String tipo, String grupo, String rh) {
        return tipo + "|" + grupo + "|" + rh;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
