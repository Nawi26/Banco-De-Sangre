package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// RF-07/RF-08: tamizaje serológico de una donación y su resultado de liberación/bloqueo.
@Entity
@Table(name = "tamizajes_serologicos")
@Getter
@Setter
@NoArgsConstructor
public class TamizajeSerologico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donacion_id", nullable = false, unique = true)
    private Donacion donacion;

    // DOBLE_DIGITACION o LIS
    @Column(nullable = false)
    private String origen;

    // PENDIENTE_SEGUNDA_DIGITACION, DISCORDANTE, CONFIRMADO
    @Column(nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario1_id")
    private Usuario usuario1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario2_id")
    private Usuario usuario2;

    @Column(name = "fecha_primera_digitacion")
    private LocalDateTime fechaPrimeraDigitacion;

    @Column(name = "fecha_segunda_digitacion")
    private LocalDateTime fechaSegundaDigitacion;

    // NO_REACTIVO (libera), REACTIVO o INDETERMINADO (bloquea) — ver TamizajeSerologicoService.
    @Column(name = "resultado_general")
    private String resultadoGeneral;

    @OneToMany(mappedBy = "tamizaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoMarcador> resultados = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
