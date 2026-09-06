package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_intercambio")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudIntercambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipress_solicitante_id", nullable = false)
    private IpressEstablecimiento ipressSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipress_proveedora_id", nullable = false)
    private IpressEstablecimiento ipressProveedora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemocomponente_id", nullable = false)
    private Hemocomponente hemocomponente;

    private String estado;

    @Column(name = "temperatura_cadena_frio")
    private Double temperaturaCadenaFrio;

    @Column(name = "responsable_transporte")
    private String responsableTransporte;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}
