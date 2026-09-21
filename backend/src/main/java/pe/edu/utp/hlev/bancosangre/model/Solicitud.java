package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_solicitud")
    private String codigoSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuario medico;

    @Column(name = "tipo_hemocomponente")
    private String tipoHemocomponente;

    @Column(name = "unidades_solicitadas")
    private Integer unidadesSolicitadas;

    private String prioridad;

    private String estado;

    @Column(name = "indicacion_clinica")
    private String indicacionClinica;

    // RF-11: código CIE-10 del diagnóstico que sustenta la solicitud transfusional.
    @Column(name = "diagnostico_cie10")
    private String diagnosticoCie10;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;
}
