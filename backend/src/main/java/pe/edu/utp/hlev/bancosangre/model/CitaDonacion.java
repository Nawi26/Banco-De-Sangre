package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-22: programación, reprogramación y recordatorio de citas de donación.
@Entity
@Table(name = "citas_donacion")
@Getter
@Setter
@NoArgsConstructor
public class CitaDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campana_id")
    private CampanaDonacion campana;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    // PROGRAMADA, REPROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA, NO_ASISTIO
    @Column(nullable = false)
    private String estado = "PROGRAMADA";

    private String notas;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
