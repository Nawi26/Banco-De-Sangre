package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-25: reserva de unidades de hemocomponentes para una cirugía programada,
// con liberación automática si no se utilizan dentro del plazo posterior a la cirugía.
@Entity
@Table(name = "reservas_quirurgicas")
@Getter
@Setter
@NoArgsConstructor
public class ReservaQuirurgica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_solicitante_id", nullable = false)
    private Usuario medicoSolicitante;

    @Column(name = "tipo_hemocomponente", nullable = false)
    private String tipoHemocomponente;

    @Column(name = "grupo_abo", nullable = false)
    private String grupoAbo;

    @Column(name = "factor_rh", nullable = false)
    private String factorRh;

    @Column(name = "unidades_solicitadas", nullable = false)
    private Integer unidadesSolicitadas;

    @Column(name = "fecha_cirugia_programada", nullable = false)
    private LocalDateTime fechaCirugiaProgramada;

    @Column(name = "horas_validez_post_cirugia", nullable = false)
    private Integer horasValidezPostCirugia = 48;

    private String estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
