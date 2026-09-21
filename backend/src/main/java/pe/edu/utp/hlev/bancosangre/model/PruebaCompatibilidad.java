package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-12: prueba cruzada y rastreo de anticuerpos irregulares (RAI) de una unidad
// candidata frente a una solicitud transfusional, previas a su despacho.
@Entity
@Table(name = "pruebas_compatibilidad")
@Getter
@Setter
@NoArgsConstructor
public class PruebaCompatibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemocomponente_id", nullable = false)
    private Hemocomponente hemocomponente;

    @Column(name = "resultado_rai", nullable = false)
    private String resultadoRai;

    @Column(name = "resultado_prueba_cruzada", nullable = false)
    private String resultadoPruebaCruzada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnologo_id")
    private Usuario tecnologo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
