package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-29: motivo y evidencia fotográfica del descarte de una unidad.
@Entity
@Table(name = "descartes_hemocomponente")
@Getter
@Setter
@NoArgsConstructor
public class DescarteHemocomponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemocomponente_id", nullable = false, unique = true)
    private Hemocomponente hemocomponente;

    // CADUCIDAD, ROTURA, CONTAMINACION u OTRO
    @Column(nullable = false)
    private String motivo;

    @Column(name = "evidencia_foto", columnDefinition = "TEXT")
    private String evidenciaFoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
