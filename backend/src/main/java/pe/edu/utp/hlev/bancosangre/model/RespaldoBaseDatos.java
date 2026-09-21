package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-34: respaldo automático/programado de la base de datos, con verificación de integridad.
@Entity
@Table(name = "respaldos_base_datos")
@Getter
@Setter
@NoArgsConstructor
public class RespaldoBaseDatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "hash_integridad")
    private String hashIntegridad;

    // EN_PROGRESO, EXITOSO, FALLIDO
    @Column(nullable = false)
    private String estado;

    private String detalle;
}
