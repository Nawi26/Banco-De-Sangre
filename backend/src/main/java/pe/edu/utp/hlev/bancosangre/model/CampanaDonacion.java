package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// RF-23/RF-35: campañas externas de donación (universidades, empresas, instituciones).
@Entity
@Table(name = "campanas_donacion")
@Getter
@Setter
@NoArgsConstructor
public class CampanaDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String institucion;

    private String tipo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "meta_unidades")
    private Integer metaUnidades;

    @Column(name = "unidades_logradas", nullable = false)
    private Integer unidadesLogradas = 0;

    @Column(nullable = false)
    private String estado = "ACTIVA";

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
