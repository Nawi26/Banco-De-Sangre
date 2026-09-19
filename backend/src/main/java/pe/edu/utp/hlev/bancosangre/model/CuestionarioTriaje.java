package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// RF-03/RF-04: cuestionario de triaje clínico-epidemiológico y su resultado de aptitud/diferimiento.
@Entity
@Table(name = "cuestionarios_triaje")
@Getter
@Setter
@NoArgsConstructor
public class CuestionarioTriaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donacion_id")
    private Donacion donacion;

    @Column(name = "antecedente_its", nullable = false)
    private Boolean antecedenteIts = false;

    @Column(name = "antecedente_uso_drogas", nullable = false)
    private Boolean antecedenteUsoDrogas = false;

    @Column(name = "tatuaje_o_perforacion_reciente", nullable = false)
    private Boolean tatuajeOPerforacionReciente = false;

    @Column(name = "embarazo_o_parto_reciente", nullable = false)
    private Boolean embarazoOPartoReciente = false;

    @Column(name = "viaje_zona_endemica", nullable = false)
    private Boolean viajeZonaEndemica = false;

    @Column(name = "otros_antecedentes")
    private String otrosAntecedentes;

    @Column(name = "resultado_apto", nullable = false)
    private Boolean resultadoApto;

    @Column(name = "resultado_tipo_diferimiento")
    private String resultadoTipoDiferimiento;

    @Column(name = "resultado_motivo")
    private String resultadoMotivo;

    @Column(name = "resultado_diferido_hasta")
    private LocalDate resultadoDiferidoHasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id")
    private Usuario evaluador;

    @Column(name = "fecha_evaluacion", nullable = false, updatable = false)
    private LocalDateTime fechaEvaluacion = LocalDateTime.now();
}
