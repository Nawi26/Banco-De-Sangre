package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * RF-17 / RNF-04: registro de auditoría inmutable.
 * Sólo se inserta (ver AuditoriaLogRepository, que no expone update/delete).
 */
@Entity
@Table(name = "auditoria_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_identificador")
    private String usuarioIdentificador;

    private String rol;

    @Column(name = "metodo_http", nullable = false)
    private String metodoHttp;

    @Column(nullable = false)
    private String endpoint;

    private String accion;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @Column(name = "resultado_http")
    private Integer resultadoHttp;

    @Column(nullable = false)
    private Boolean exitoso;

    private String detalle;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
