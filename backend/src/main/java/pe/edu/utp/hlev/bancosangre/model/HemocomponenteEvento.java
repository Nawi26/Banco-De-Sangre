package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-30: evento del ciclo de vida de una unidad (fraccionamiento, liberación, bloqueo,
// reserva, despacho, descarte, transferencia), para reconstruir su trazabilidad completa.
@Entity
@Table(name = "hemocomponente_eventos")
@Getter
@Setter
@NoArgsConstructor
public class HemocomponenteEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemocomponente_id", nullable = false)
    private Hemocomponente hemocomponente;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    private String detalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
