package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-15: hemovigilancia — evento/incidente adverso transfusional (inmediato o tardío),
// trazable hacia la unidad y, a través de ella, hacia la donación de origen.
@Entity
@Table(name = "eventos_adversos_transfusion")
@Getter
@Setter
@NoArgsConstructor
public class EventoAdversoTransfusional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfusion_id", nullable = false)
    private Transfusion transfusion;

    @Column(name = "tipo_reaccion", nullable = false)
    private String tipoReaccion;

    @Column(name = "es_inmediata", nullable = false)
    private Boolean esInmediata;

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
