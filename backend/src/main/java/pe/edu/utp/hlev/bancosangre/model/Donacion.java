package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "donaciones")
@Getter
@Setter
@NoArgsConstructor
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "din_isbt128")
    private String dinIsbt128;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    @Column(name = "fecha_extraccion")
    private LocalDateTime fechaExtraccion;

    @Column(name = "volumen_ml")
    private Integer volumenMl;

    @Column(name = "tipo_donacion")
    private String tipoDonacion;

    @Column(name = "tamizaje_aprobado")
    private Boolean tamizajeAprobado;
}
