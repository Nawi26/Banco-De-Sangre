package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// RF-24: lectura de temperatura de una cámara (integración con sensores) y su alerta de desviación.
@Entity
@Table(name = "registros_temperatura")
@Getter
@Setter
@NoArgsConstructor
public class RegistroTemperatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camara_id", nullable = false)
    private CamaraAlmacenamiento camara;

    @Column(nullable = false)
    private BigDecimal temperatura;

    @Column(name = "dentro_de_rango", nullable = false)
    private Boolean dentroDeRango;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn = LocalDateTime.now();
}
