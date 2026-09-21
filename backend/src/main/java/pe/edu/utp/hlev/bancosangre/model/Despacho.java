package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-13: doble verificación electrónica del despacho de una unidad hacia una solicitud
// transfusional. Dos responsables distintos deben confirmar (firma electrónica) antes
// de que la unidad salga del banco de sangre.
@Entity
@Table(name = "despachos")
@Getter
@Setter
@NoArgsConstructor
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemocomponente_id", nullable = false)
    private Hemocomponente hemocomponente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primera_verificacion_usuario_id", nullable = false)
    private Usuario primeraVerificacionUsuario;

    @Column(name = "primera_verificacion_en", nullable = false)
    private LocalDateTime primeraVerificacionEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segunda_verificacion_usuario_id")
    private Usuario segundaVerificacionUsuario;

    @Column(name = "segunda_verificacion_en")
    private LocalDateTime segundaVerificacionEn;

    private String estado;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;
}
