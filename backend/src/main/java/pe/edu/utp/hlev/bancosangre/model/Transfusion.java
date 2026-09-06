package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfusiones")
@Getter
@Setter
@NoArgsConstructor
public class Transfusion {

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
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnologo_id")
    private Usuario tecnologo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Usuario medico;

    @Column(name = "resultado_prueba_cruzada")
    private String resultadoPruebaCruzada;

    @Column(name = "fecha_transfusion")
    private LocalDateTime fechaTransfusion;

    @Column(name = "reaccion_adversa")
    private Boolean reaccionAdversa;

    @Column(name = "detalles_reaccion")
    private String detallesReaccion;
}
