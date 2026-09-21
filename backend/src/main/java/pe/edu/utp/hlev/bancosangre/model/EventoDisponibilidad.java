package pe.edu.utp.hlev.bancosangre.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// RF-39: monitoreo de disponibilidad (uptime) de la plataforma: arranques y apagados registrados.
@Entity
@Table(name = "eventos_disponibilidad")
@Getter
@Setter
@NoArgsConstructor
public class EventoDisponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INICIO o APAGADO
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
