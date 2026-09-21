package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-43: hemovigilancia — evento adverso ocurrido durante o después de la donación
// (síncope, hematoma, mareo), asociado al donante y a la unidad extraída.
@Entity
@Table(name = "eventos_adversos_donacion")
@Getter
@Setter
@NoArgsConstructor
public class EventoAdversoDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donacion_id", nullable = false)
    private Donacion donacion;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    private String gravedad;

    private String descripcion;

    @Column(name = "acciones_tomadas")
    private String accionesTomadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_deteccion", nullable = false, updatable = false)
    private LocalDateTime fechaDeteccion = LocalDateTime.now();
}
