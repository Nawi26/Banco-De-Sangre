package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CrearReservaQuirurgicaRequest;
import pe.edu.utp.hlev.bancosangre.dto.ReservaQuirurgicaDTO;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Paciente;
import pe.edu.utp.hlev.bancosangre.model.ReservaQuirurgica;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.PacienteRepository;
import pe.edu.utp.hlev.bancosangre.repository.ReservaQuirurgicaRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-25: reserva de unidades para una cirugía programada. Si la cirugía ya pasó y la
 * reserva sigue vigente (no fue marcada como utilizada), las unidades se liberan
 * automáticamente de vuelta al inventario disponible.
 */
@Service
public class ReservaQuirurgicaService {

    private static final String ESTADO_RESERVADA = "RESERVADA";
    private static final String ESTADO_UTILIZADA = "UTILIZADA";
    private static final String ESTADO_LIBERADA = "LIBERADA";
    private static final int HORAS_VALIDEZ_POR_DEFECTO = 48;

    private final ReservaQuirurgicaRepository reservaQuirurgicaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HemocomponenteRepository hemocomponenteRepository;
    private final HemocomponenteEventoService hemocomponenteEventoService;

    public ReservaQuirurgicaService(ReservaQuirurgicaRepository reservaQuirurgicaRepository,
                                     PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository,
                                     HemocomponenteRepository hemocomponenteRepository,
                                     HemocomponenteEventoService hemocomponenteEventoService) {
        this.reservaQuirurgicaRepository = reservaQuirurgicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.hemocomponenteEventoService = hemocomponenteEventoService;
    }

    public List<ReservaQuirurgicaDTO> listar() {
        return reservaQuirurgicaRepository.listarConDetalle().stream().map(ReservaQuirurgicaDTO::from).toList();
    }

    @Transactional
    public ReservaQuirurgicaDTO reservar(CrearReservaQuirurgicaRequest request, Long medicoId) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Paciente no encontrado."));
        Usuario medico = usuarioRepository.findById(medicoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Médico no encontrado."));

        String tipo = request.tipoHemocomponente();
        String grupo = request.grupoAbo().trim().toUpperCase();
        String rh = request.factorRh().trim().toUpperCase();

        List<Hemocomponente> candidatas = hemocomponenteRepository
                .findByEstadoAndTipoHemocomponenteAndGrupoAboAndFactorRhOrderByFechaVencimientoAsc(
                        EstadoHemocomponente.DISPONIBLE, tipo, grupo, rh);

        if (candidatas.size() < request.unidadesSolicitadas()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Stock insuficiente: hay " + candidatas.size() + " unidad(es) disponible(s) de " + tipo + " " + grupo + rh
                            + ", se requieren " + request.unidadesSolicitadas() + ".");
        }

        ReservaQuirurgica reserva = new ReservaQuirurgica();
        reserva.setPaciente(paciente);
        reserva.setMedicoSolicitante(medico);
        reserva.setTipoHemocomponente(tipo);
        reserva.setGrupoAbo(grupo);
        reserva.setFactorRh(rh);
        reserva.setUnidadesSolicitadas(request.unidadesSolicitadas());
        reserva.setFechaCirugiaProgramada(request.fechaCirugiaProgramada());
        reserva.setHorasValidezPostCirugia(
                request.horasValidezPostCirugia() != null ? request.horasValidezPostCirugia() : HORAS_VALIDEZ_POR_DEFECTO);
        reserva.setEstado(ESTADO_RESERVADA);
        reservaQuirurgicaRepository.save(reserva);

        List<Hemocomponente> asignadas = candidatas.subList(0, request.unidadesSolicitadas());
        for (Hemocomponente hemocomponente : asignadas) {
            hemocomponente.setEstado(EstadoHemocomponente.RESERVADO_QUIRURGICO);
            hemocomponente.setReservaQuirurgica(reserva);
            hemocomponenteEventoService.registrar(hemocomponente, "RESERVA_QUIRURGICA",
                    "Apartada para la cirugía programada de " + paciente.getNombres() + " " + paciente.getApellidos()
                            + " el " + request.fechaCirugiaProgramada() + ".", medicoId);
        }
        hemocomponenteRepository.saveAll(asignadas);

        return ReservaQuirurgicaDTO.from(reserva);
    }

    /** Liberación manual antes de la cirugía (ej. se canceló o se descartó la necesidad). */
    @Transactional
    public void liberar(Long id, Long usuarioId) {
        ReservaQuirurgica reserva = reservaQuirurgicaRepository.buscarPorIdConDetalle(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reserva quirúrgica no encontrada."));

        if (!ESTADO_RESERVADA.equals(reserva.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Sólo se puede liberar una reserva vigente.");
        }

        liberarUnidades(reserva, usuarioId, "Liberación manual de la reserva quirúrgica.");
        reserva.setEstado(ESTADO_LIBERADA);
        reservaQuirurgicaRepository.save(reserva);
    }

    /** El personal del banco de sangre confirma que las unidades reservadas se usaron en la cirugía. */
    @Transactional
    public void marcarUtilizada(Long id) {
        ReservaQuirurgica reserva = reservaQuirurgicaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reserva quirúrgica no encontrada."));

        if (!ESTADO_RESERVADA.equals(reserva.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Sólo se puede marcar como utilizada una reserva vigente.");
        }
        reserva.setEstado(ESTADO_UTILIZADA);
        reservaQuirurgicaRepository.save(reserva);
    }

    /**
     * RF-25: tarea periódica que libera automáticamente las reservas cuya cirugía ya pasó
     * (más el margen de {@code horasValidezPostCirugia}) y que nunca se marcaron como utilizadas.
     */
    @Scheduled(fixedRate = 900_000)
    @Transactional
    public void liberarVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<ReservaQuirurgica> vencidas = reservaQuirurgicaRepository.findByEstado(ESTADO_RESERVADA).stream()
                .filter(r -> r.getFechaCirugiaProgramada().plusHours(r.getHorasValidezPostCirugia()).isBefore(ahora))
                .toList();

        for (ReservaQuirurgica reserva : vencidas) {
            liberarUnidades(reserva, null,
                    "Liberación automática: venció el plazo posterior a la cirugía programada sin haberse utilizado.");
            reserva.setEstado(ESTADO_LIBERADA);
        }
        reservaQuirurgicaRepository.saveAll(vencidas);
    }

    private void liberarUnidades(ReservaQuirurgica reserva, Long usuarioId, String motivo) {
        List<Hemocomponente> unidades = hemocomponenteRepository.findByReservaQuirurgicaId(reserva.getId());
        for (Hemocomponente hemocomponente : unidades) {
            if (EstadoHemocomponente.RESERVADO_QUIRURGICO.equals(hemocomponente.getEstado())) {
                hemocomponente.setEstado(EstadoHemocomponente.DISPONIBLE);
                hemocomponente.setReservaQuirurgica(null);
                hemocomponenteEventoService.registrar(hemocomponente, "RESERVA_QUIRURGICA_LIBERADA", motivo, usuarioId);
            }
        }
        hemocomponenteRepository.saveAll(unidades);
    }
}
