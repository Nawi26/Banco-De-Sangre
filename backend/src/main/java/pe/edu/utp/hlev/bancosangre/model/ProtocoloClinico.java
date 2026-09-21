package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-49: control de versiones de protocolos y guías clínicas vigentes para validar solicitudes.
@Entity
@Table(name = "protocolos_clinicos")
@Getter
@Setter
@NoArgsConstructor
public class ProtocoloClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String version;

    @Column(name = "contenido_url")
    private String contenidoUrl;

    @Column(name = "notas_cambio")
    private String notasCambio;

    @Column(nullable = false)
    private Boolean vigente = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicado_por_id")
    private Usuario publicadoPor;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();
}
