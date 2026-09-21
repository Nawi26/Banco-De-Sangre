package pe.edu.utp.hlev.bancosangre.service;

import jakarta.annotation.PreDestroy;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;
import pe.edu.utp.hlev.bancosangre.dto.DisponibilidadDTO;
import pe.edu.utp.hlev.bancosangre.dto.EventoDisponibilidadDTO;
import pe.edu.utp.hlev.bancosangre.model.EventoDisponibilidad;
import pe.edu.utp.hlev.bancosangre.repository.EventoDisponibilidadRepository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * RF-39: monitoreo de disponibilidad (uptime) de la plataforma. Registra cada arranque
 * y apagado ordenado del backend para reconstruir el historial de tiempos de actividad
 * e inactividad. Nota: un apagado abrupto (caída del proceso, corte de energía) no
 * dispara {@code @PreDestroy}, por lo que ese escenario sólo queda evidenciado por el
 * siguiente evento de INICIO sin un APAGADO previo correspondiente.
 */
@Service
public class DisponibilidadService {

    private static final String TIPO_INICIO = "INICIO";
    private static final String TIPO_APAGADO = "APAGADO";

    private final EventoDisponibilidadRepository eventoDisponibilidadRepository;

    public DisponibilidadService(EventoDisponibilidadRepository eventoDisponibilidadRepository) {
        this.eventoDisponibilidadRepository = eventoDisponibilidadRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registrarInicio() {
        EventoDisponibilidad evento = new EventoDisponibilidad();
        evento.setTipo(TIPO_INICIO);
        eventoDisponibilidadRepository.save(evento);
    }

    @PreDestroy
    public void registrarApagado() {
        EventoDisponibilidad evento = new EventoDisponibilidad();
        evento.setTipo(TIPO_APAGADO);
        eventoDisponibilidadRepository.save(evento);
    }

    public DisponibilidadDTO obtenerEstado() {
        LocalDateTime enLineaDesde = eventoDisponibilidadRepository.findFirstByTipoOrderByFechaDesc(TIPO_INICIO)
                .map(EventoDisponibilidad::getFecha)
                .orElse(null);

        long segundosActivo = enLineaDesde != null ? Duration.between(enLineaDesde, LocalDateTime.now()).toSeconds() : 0;

        var historial = eventoDisponibilidadRepository.findAllByOrderByFechaDesc().stream()
                .map(EventoDisponibilidadDTO::from).toList();

        return new DisponibilidadDTO(enLineaDesde, segundosActivo, historial);
    }
}
