package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-36: snapshot de un reporte normativo PRONAHEBAS generado automáticamente
// (histórico de presentaciones periódicas ante el MINSA).
@Entity
@Table(name = "reportes_normativos_generados")
@Getter
@Setter
@NoArgsConstructor
public class ReporteNormativoGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "periodo_desde", nullable = false)
    private LocalDateTime periodoDesde;

    @Column(name = "periodo_hasta", nullable = false)
    private LocalDateTime periodoHasta;

    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "donaciones_registradas", nullable = false)
    private Long donacionesRegistradas;

    @Column(name = "transfusiones_realizadas", nullable = false)
    private Long transfusionesRealizadas;

    @Column(name = "descartes_total", nullable = false)
    private Long descartesTotal;

    @Column(name = "eventos_adversos_total", nullable = false)
    private Long eventosAdversosTotal;

    @Column(name = "certificados_emitidos", nullable = false)
    private Long certificadosEmitidos;
}
