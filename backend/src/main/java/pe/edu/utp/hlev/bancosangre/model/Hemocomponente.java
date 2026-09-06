package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hemocomponentes")
@Getter
@Setter
@NoArgsConstructor
public class Hemocomponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_producto_isbt")
    private String codigoProductoIsbt;

    @Column(name = "tipo_hemocomponente")
    private String tipoHemocomponente;

    @Column(name = "grupo_abo")
    private String grupoAbo;

    @Column(name = "factor_rh")
    private String factorRh;

    @Column(name = "volumen_ml")
    private Integer volumenMl;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "ubicacion_fisica")
    private String ubicacionFisica;

    private String estado;
}
