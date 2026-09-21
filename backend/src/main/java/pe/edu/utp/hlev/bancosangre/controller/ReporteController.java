package pe.edu.utp.hlev.bancosangre.controller;

import pe.edu.utp.hlev.bancosangre.dto.IndicadorCumplimientoDTO;
import pe.edu.utp.hlev.bancosangre.dto.IndicadoresKpiDTO;
import pe.edu.utp.hlev.bancosangre.dto.ReporteNormativoDTO;
import pe.edu.utp.hlev.bancosangre.dto.ReporteOperacionalDTO;
import pe.edu.utp.hlev.bancosangre.security.SoloSupervisionBancoSangre;
import pe.edu.utp.hlev.bancosangre.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-16/RF-26/RF-27/RF-36/RF-44/RF-50: reportes gerenciales, de cumplimiento normativo,
 * operacionales exportables y datasets anonimizados. Reservado a nivel de supervisión
 * (Jefe de Banco de Sangre / Administrador), acorde al carácter gerencial/regulatorio.
 */
@RestController
@RequestMapping("/api/reportes")
@SoloSupervisionBancoSangre
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/kpis")
    public IndicadoresKpiDTO kpis(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        LocalDateTime[] rango = normalizarRango(desde, hasta);
        return reporteService.calcularKpis(rango[0], rango[1]);
    }

    @GetMapping("/cumplimiento")
    public List<IndicadorCumplimientoDTO> cumplimiento() {
        return reporteService.calcularCumplimiento();
    }

    @GetMapping("/operacional")
    public ReporteOperacionalDTO operacional(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        LocalDateTime[] rango = normalizarRango(desde, hasta);
        return reporteService.generarReporteOperacional(rango[0], rango[1]);
    }

    // RF-27: exportación en formato compatible con Excel (CSV delimitado por ";").
    @GetMapping("/operacional/csv")
    public ResponseEntity<byte[]> operacionalCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        LocalDateTime[] rango = normalizarRango(desde, hasta);
        String csv = reporteService.generarCsvReporteOperacional(rango[0], rango[1]);
        return archivoDescargable(csv, "reporte-operacional.csv");
    }

    // RF-36: historial de reportes normativos generados automáticamente (y disparo manual para pruebas/demo).
    @GetMapping("/normativos/historial")
    public List<ReporteNormativoDTO> historialNormativo() {
        return reporteService.listarHistorialNormativo();
    }

    @PostMapping("/normativos/generar")
    public ReporteNormativoDTO generarNormativoManual(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteService.generarReporteNormativoManual(desde, hasta);
    }

    // RF-50: dataset anonimizado de donantes (sin nombre, documento, teléfono ni dirección).
    @GetMapping("/dataset-anonimizado/csv")
    public ResponseEntity<byte[]> datasetAnonimizadoCsv() {
        String csv = reporteService.generarCsvDatasetAnonimizado();
        return archivoDescargable(csv, "dataset-anonimizado-donantes.csv");
    }

    private LocalDateTime[] normalizarRango(LocalDateTime desde, LocalDateTime hasta) {
        LocalDateTime finReal = hasta != null ? hasta : LocalDateTime.now();
        LocalDateTime inicioReal = desde != null ? desde : finReal.minusDays(30);
        return new LocalDateTime[]{inicioReal, finReal};
    }

    private ResponseEntity<byte[]> archivoDescargable(String contenido, String nombreArchivo) {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}; // BOM para que Excel detecte UTF-8.
        byte[] cuerpo = contenido.getBytes(StandardCharsets.UTF_8);
        byte[] archivo = new byte[bom.length + cuerpo.length];
        System.arraycopy(bom, 0, archivo, 0, bom.length);
        System.arraycopy(cuerpo, 0, archivo, bom.length, cuerpo.length);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(archivo);
    }
}
