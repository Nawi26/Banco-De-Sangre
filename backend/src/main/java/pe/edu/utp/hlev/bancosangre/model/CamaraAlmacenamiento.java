package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// RF-09/RF-24: cámaras de refrigeración o congeladoras donde se almacenan los hemocomponentes.
@Entity
@Table(name = "camaras_almacenamiento")
@Getter
@Setter
@NoArgsConstructor
public class CamaraAlmacenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // REFRIGERACION o CONGELACION
    private String tipo;

    private String ubicacion;

    @Column(name = "temperatura_minima")
    private BigDecimal temperaturaMinima;

    @Column(name = "temperatura_maxima")
    private BigDecimal temperaturaMaxima;

    @Column(nullable = false)
    private Boolean activa = true;
}
