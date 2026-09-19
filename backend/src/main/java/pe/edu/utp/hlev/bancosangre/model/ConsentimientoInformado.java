package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-38: consentimiento informado digital del donante previo a la extracción.
@Entity
@Table(name = "consentimientos_informados")
@Getter
@Setter
@NoArgsConstructor
public class ConsentimientoInformado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donacion_id")
    private Donacion donacion;

    // FIRMA o BIOMETRICO
    @Column(name = "tipo_validacion", nullable = false)
    private String tipoValidacion;

    // Firma digital (base64) o referencia/hash de la validación biométrica.
    @Column(name = "evidencia_validacion", nullable = false, columnDefinition = "TEXT")
    private String evidenciaValidacion;

    @Column(nullable = false)
    private Boolean aceptado;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
