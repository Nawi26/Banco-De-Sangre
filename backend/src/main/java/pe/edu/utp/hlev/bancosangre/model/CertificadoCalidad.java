package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-41: certificado de calidad de una unidad procesada, con código de verificación (QR).
@Entity
@Table(name = "certificados_calidad")
@Getter
@Setter
@NoArgsConstructor
public class CertificadoCalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemocomponente_id", nullable = false, unique = true)
    private Hemocomponente hemocomponente;

    @Column(name = "numero_certificado", nullable = false, unique = true)
    private String numeroCertificado;

    @Column(name = "codigo_verificacion", nullable = false, unique = true)
    private String codigoVerificacion;

    @Column(name = "emitido_en", nullable = false, updatable = false)
    private LocalDateTime emitidoEn = LocalDateTime.now();
}
