package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// RF-07: resultado de un marcador infeccioso dentro de un tamizaje (con doble digitación ciega).
@Entity
@Table(name = "resultados_marcador")
@Getter
@Setter
@NoArgsConstructor
public class ResultadoMarcador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tamizaje_id", nullable = false)
    private TamizajeSerologico tamizaje;

    @Column(nullable = false)
    private String marcador;

    @Column(name = "resultado_digitacion1")
    private String resultadoDigitacion1;

    @Column(name = "resultado_digitacion2")
    private String resultadoDigitacion2;

    @Column(name = "resultado_final")
    private String resultadoFinal;

    private Boolean concordante;
}
