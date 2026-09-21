package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-47: mantenimiento preventivo y calibración de equipos críticos.
@Entity
@Table(name = "mantenimientos_equipo")
@Getter
@Setter
@NoArgsConstructor
public class MantenimientoEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_equipo", nullable = false)
    private String nombreEquipo;

    @Column(name = "tipo_equipo", nullable = false)
    private String tipoEquipo;

    @Column(name = "tipo_mantenimiento", nullable = false)
    private String tipoMantenimiento;

    @Column(name = "fecha_realizado", nullable = false)
    private LocalDateTime fechaRealizado;

    @Column(name = "fecha_proximo_vencimiento")
    private LocalDateTime fechaProximoVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    private String observaciones;
}
