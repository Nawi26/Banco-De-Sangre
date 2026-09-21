package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.IndicadorCumplimientoDTO;
import pe.edu.utp.hlev.bancosangre.dto.IndicadoresKpiDTO;
import pe.edu.utp.hlev.bancosangre.dto.ReporteNormativoDTO;
import pe.edu.utp.hlev.bancosangre.dto.ReporteOperacionalDTO;
import pe.edu.utp.hlev.bancosangre.model.DescarteHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.ReporteNormativoGenerado;
import pe.edu.utp.hlev.bancosangre.model.Transfusion;
import pe.edu.utp.hlev.bancosangre.repository.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * RF-16/RF-26/RF-36/RF-44: reportes operacionales/estadísticos, KPIs gerenciales e
 * indicadores de cumplimiento normativo PRONAHEBAS, todos calculados en tiempo real
 * a partir de los mismos datos operativos (sin tablas de reporte duplicadas), salvo
 * el historial de generación automática periódica (RF-36).
 */
@Service
public class ReporteService {

    private static final List<String> ESTADOS_ACTIVOS = EstadoHemocomponente.ESTADOS_INVENTARIO_ACTIVO;
    private static final String TIPO_EVENTO_FRACCIONAMIENTO = "FRACCIONAMIENTO";

    private final HemocomponenteRepository hemocomponenteRepository;
    private final HemocomponenteEventoRepository hemocomponenteEventoRepository;
    private final DonacionRepository donacionRepository;
    private final DonanteRepository donanteRepository;
    private final TransfusionRepository transfusionRepository;
    private final DescarteHemocomponenteRepository descarteHemocomponenteRepository;
    private final SolicitudRepository solicitudRepository;
    private final CertificadoCalidadRepository certificadoCalidadRepository;
    private final EventoAdversoTransfusionalRepository eventoAdversoTransfusionalRepository;
    private final EventoAdversoDonacionRepository eventoAdversoDonacionRepository;
    private final ConsentimientoInformadoRepository consentimientoInformadoRepository;
    private final AuditoriaLogRepository auditoriaLogRepository;
    private final ReporteNormativoGeneradoRepository reporteNormativoGeneradoRepository;

    public ReporteService(HemocomponenteRepository hemocomponenteRepository,
                           HemocomponenteEventoRepository hemocomponenteEventoRepository,
                           DonacionRepository donacionRepository, DonanteRepository donanteRepository,
                           TransfusionRepository transfusionRepository,
                           DescarteHemocomponenteRepository descarteHemocomponenteRepository,
                           SolicitudRepository solicitudRepository,
                           CertificadoCalidadRepository certificadoCalidadRepository,
                           EventoAdversoTransfusionalRepository eventoAdversoTransfusionalRepository,
                           EventoAdversoDonacionRepository eventoAdversoDonacionRepository,
                           ConsentimientoInformadoRepository consentimientoInformadoRepository,
                           AuditoriaLogRepository auditoriaLogRepository,
                           ReporteNormativoGeneradoRepository reporteNormativoGeneradoRepository) {
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.hemocomponenteEventoRepository = hemocomponenteEventoRepository;
        this.donacionRepository = donacionRepository;
        this.donanteRepository = donanteRepository;
        this.transfusionRepository = transfusionRepository;
        this.descarteHemocomponenteRepository = descarteHemocomponenteRepository;
        this.solicitudRepository = solicitudRepository;
        this.certificadoCalidadRepository = certificadoCalidadRepository;
        this.eventoAdversoTransfusionalRepository = eventoAdversoTransfusionalRepository;
        this.eventoAdversoDonacionRepository = eventoAdversoDonacionRepository;
        this.consentimientoInformadoRepository = consentimientoInformadoRepository;
        this.auditoriaLogRepository = auditoriaLogRepository;
        this.reporteNormativoGeneradoRepository = reporteNormativoGeneradoRepository;
    }

    // RF-26: rotación de inventario, mermas por caducidad y tiempo de respuesta, en tiempo real.
    public IndicadoresKpiDTO calcularKpis(LocalDateTime desde, LocalDateTime hasta) {
        long unidadesFraccionadas = hemocomponenteEventoRepository.countByTipoEventoAndFechaBetween(TIPO_EVENTO_FRACCIONAMIENTO, desde, hasta);
        long unidadesTransfundidas = transfusionRepository.countByFechaTransfusionBetween(desde, hasta);
        long inventarioActivo = hemocomponenteRepository.countByEstadoIn(ESTADOS_ACTIVOS);

        double rotacion = inventarioActivo > 0 ? (double) unidadesTransfundidas / inventarioActivo : 0.0;

        long descartesPorCaducidad = descarteHemocomponenteRepository.findByFechaBetween(desde, hasta).stream()
                .filter(d -> "CADUCIDAD".equals(d.getMotivo()))
                .count();
        double tasaMerma = unidadesFraccionadas > 0 ? (descartesPorCaducidad * 100.0) / unidadesFraccionadas : 0.0;

        List<Transfusion> transfusionesEnRango = transfusionRepository.listarEnRangoConSolicitud(desde, hasta);
        OptionalDouble promedioMinutos = transfusionesEnRango.stream()
                .filter(t -> t.getSolicitud().getFechaSolicitud() != null)
                .mapToLong(t -> Duration.between(t.getSolicitud().getFechaSolicitud(), t.getFechaTransfusion()).toMinutes())
                .average();

        return new IndicadoresKpiDTO(
                rotacion,
                tasaMerma,
                promedioMinutos.isPresent() ? promedioMinutos.getAsDouble() : null,
                unidadesTransfundidas,
                unidadesFraccionadas,
                inventarioActivo
        );
    }

    // RF-44: indicadores de cumplimiento normativo PRONAHEBAS, actualizados en tiempo real.
    public List<IndicadorCumplimientoDTO> calcularCumplimiento() {
        long totalDonaciones = donacionRepository.count();
        long donacionesTamizadas = donacionRepository.countByTamizajeAprobadoIsNotNull();
        double pctTamizaje = totalDonaciones > 0 ? donacionesTamizadas * 100.0 / totalDonaciones : 100.0;

        long totalDescartes = descarteHemocomponenteRepository.count();
        long descartesConEvidencia = descarteHemocomponenteRepository.countByEvidenciaFotoIsNotNull();
        double pctEvidencia = totalDescartes > 0 ? descartesConEvidencia * 100.0 / totalDescartes : 100.0;

        long totalConsentimientos = consentimientoInformadoRepository.count();
        double pctConsentimiento = totalDonaciones > 0 ? Math.min(100.0, totalConsentimientos * 100.0 / totalDonaciones) : 100.0;

        long totalCertificados = certificadoCalidadRepository.count();

        return List.of(
                new IndicadorCumplimientoDTO("TAMIZAJE_COMPLETO", "Donaciones con tamizaje serológico completado",
                        redondear(pctTamizaje), "RF-07/RF-08: porcentaje de donaciones con resultado de tamizaje ya registrado."),
                new IndicadorCumplimientoDTO("DESCARTE_CON_EVIDENCIA", "Descartes con evidencia fotográfica adjunta",
                        redondear(pctEvidencia), "RF-29: porcentaje de descartes que cuentan con respaldo fotográfico."),
                new IndicadorCumplimientoDTO("CONSENTIMIENTO_INFORMADO", "Donantes con consentimiento informado digital",
                        redondear(pctConsentimiento), "RF-38: consentimientos registrados frente al total de donaciones."),
                new IndicadorCumplimientoDTO("AUDITORIA_ACTIVA", "Acciones críticas registradas en la bitácora de auditoría",
                        auditoriaLogRepository.contarTodos(), "RF-17/RNF-04: total histórico de eventos auditados (no es un porcentaje)."),
                new IndicadorCumplimientoDTO("CERTIFICACION_CALIDAD", "Certificados de calidad emitidos",
                        totalCertificados, "RF-41: total histórico de unidades con certificado de calidad y QR emitido.")
        );
    }

    // RF-16/RF-36: reporte operacional/estadístico consolidado del período.
    public ReporteOperacionalDTO generarReporteOperacional(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, Long> descartesPorMotivo = descarteHemocomponenteRepository.findByFechaBetween(desde, hasta).stream()
                .collect(Collectors.groupingBy(DescarteHemocomponente::getMotivo, LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> solicitudesPorEstado = new LinkedHashMap<>();
        for (Object[] fila : solicitudRepository.contarPorEstadoEnRango(desde, hasta)) {
            solicitudesPorEstado.put((String) fila[0], (Long) fila[1]);
        }

        return new ReporteOperacionalDTO(
                desde, hasta,
                donacionRepository.countByFechaExtraccionBetween(desde, hasta),
                transfusionRepository.countByFechaTransfusionBetween(desde, hasta),
                descartesPorMotivo,
                eventoAdversoTransfusionalRepository.countByFechaDeteccionBetween(desde, hasta),
                eventoAdversoDonacionRepository.countByFechaDeteccionBetween(desde, hasta),
                solicitudesPorEstado,
                certificadoCalidadRepository.countByEmitidoEnBetween(desde, hasta)
        );
    }

    public List<ReporteNormativoDTO> listarHistorialNormativo() {
        return reporteNormativoGeneradoRepository.findAllByOrderByFechaGeneracionDesc().stream()
                .map(ReporteNormativoDTO::from).toList();
    }

    /** RF-36: genera automáticamente el reporte normativo del mes anterior, el día 1 de cada mes. */
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void generarReporteNormativoMensualAutomatico() {
        LocalDateTime hasta = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime desde = hasta.minusMonths(1);
        generarYGuardarReporteNormativo(desde, hasta);
    }

    @Transactional
    public ReporteNormativoDTO generarReporteNormativoManual(LocalDateTime desde, LocalDateTime hasta) {
        return generarYGuardarReporteNormativo(desde, hasta);
    }

    private ReporteNormativoDTO generarYGuardarReporteNormativo(LocalDateTime desde, LocalDateTime hasta) {
        ReporteOperacionalDTO resumen = generarReporteOperacional(desde, hasta);
        long descartesTotal = resumen.descartesPorMotivo().values().stream().mapToLong(Long::longValue).sum();

        ReporteNormativoGenerado reporte = new ReporteNormativoGenerado();
        reporte.setPeriodoDesde(desde);
        reporte.setPeriodoHasta(hasta);
        reporte.setDonacionesRegistradas(resumen.donacionesRegistradas());
        reporte.setTransfusionesRealizadas(resumen.transfusionesRealizadas());
        reporte.setDescartesTotal(descartesTotal);
        reporte.setEventosAdversosTotal(resumen.eventosAdversosTransfusionales() + resumen.eventosAdversosDonacion());
        reporte.setCertificadosEmitidos(resumen.certificadosEmitidos());
        reporteNormativoGeneradoRepository.save(reporte);

        return ReporteNormativoDTO.from(reporte);
    }

    // RF-16/RF-27: reporte operacional en CSV (abre directamente en Excel).
    public String generarCsvReporteOperacional(LocalDateTime desde, LocalDateTime hasta) {
        ReporteOperacionalDTO r = generarReporteOperacional(desde, hasta);
        StringBuilder csv = new StringBuilder();
        csv.append("Reporte operacional HLEV;").append(desde).append(";").append(hasta).append("\n\n");
        csv.append("Indicador;Valor\n");
        csv.append("Donaciones registradas;").append(r.donacionesRegistradas()).append("\n");
        csv.append("Transfusiones realizadas;").append(r.transfusionesRealizadas()).append("\n");
        csv.append("Eventos adversos transfusionales;").append(r.eventosAdversosTransfusionales()).append("\n");
        csv.append("Eventos adversos de donación;").append(r.eventosAdversosDonacion()).append("\n");
        csv.append("Certificados de calidad emitidos;").append(r.certificadosEmitidos()).append("\n\n");
        csv.append("Descartes por motivo\nMotivo;Cantidad\n");
        r.descartesPorMotivo().forEach((motivo, cantidad) -> csv.append(motivo).append(";").append(cantidad).append("\n"));
        csv.append("\nSolicitudes por estado\nEstado;Cantidad\n");
        r.solicitudesPorEstado().forEach((estado, cantidad) -> csv.append(estado).append(";").append(cantidad).append("\n"));
        return csv.toString();
    }

    // RF-50: dataset anonimizado (sin nombres, documento, teléfono ni dirección) con fines epidemiológicos.
    public String generarCsvDatasetAnonimizado() {
        StringBuilder csv = new StringBuilder();
        csv.append("id_anonimo;edad;sexo;grupo_abo;factor_rh;ocupacion_riesgo;apto;tipo_diferimiento\n");

        List<Donante> donantes = donanteRepository.findAllByOrderByIdDesc();
        long contador = 1;
        for (Donante d : donantes) {
            Integer edad = d.getFechaNacimiento() != null ? Period.between(d.getFechaNacimiento(), LocalDate.now()).getYears() : null;
            csv.append(contador++).append(";")
                    .append(edad != null ? edad : "").append(";")
                    .append(vacioSiNulo(d.getSexo())).append(";")
                    .append(vacioSiNulo(d.getGrupoAbo())).append(";")
                    .append(vacioSiNulo(d.getFactorRh())).append(";")
                    .append(Boolean.TRUE.equals(d.getEsOcupacionRiesgo())).append(";")
                    .append(d.getApto() != null ? d.getApto() : true).append(";")
                    .append(vacioSiNulo(d.getTipoDiferimiento())).append("\n");
        }
        return csv.toString();
    }

    private String vacioSiNulo(String valor) {
        return valor != null ? valor : "";
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
