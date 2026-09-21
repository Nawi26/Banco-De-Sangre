package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-45: convenio/contrato interinstitucional vigente para el intercambio de hemocomponentes.
@Entity
@Table(name = "convenios_interinstitucionales")
@Getter
@Setter
@NoArgsConstructor
public class ConvenioInterinstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipress_id", nullable = false)
    private IpressEstablecimiento ipress;

    @Column(name = "numero_convenio", nullable = false)
    private String numeroConvenio;

    @Column(nullable = false)
    private String objeto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    // VIGENTE, VENCIDO o RESCINDIDO
    @Column(nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
