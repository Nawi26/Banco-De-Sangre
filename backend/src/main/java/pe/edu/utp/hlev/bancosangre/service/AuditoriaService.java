package pe.edu.utp.hlev.bancosangre.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.edu.utp.hlev.bancosangre.dto.AuditoriaLogDTO;
import pe.edu.utp.hlev.bancosangre.model.AuditoriaLog;
import pe.edu.utp.hlev.bancosangre.repository.AuditoriaLogRepository;

/**
 * RF-17 / RNF-04: registra toda acción crítica con fecha/hora exacta, IP y usuario responsable.
 * Sólo inserta filas (ver AuditoriaLogRepository); nunca actualiza ni elimina.
 */
@Service
public class AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    public AuditoriaService(AuditoriaLogRepository auditoriaLogRepository) {
        this.auditoriaLogRepository = auditoriaLogRepository;
    }

    public void registrar(Long usuarioId, String usuarioIdentificador, String rol, String metodoHttp,
                           String endpoint, String accion, String ipOrigen, Integer resultadoHttp,
                           boolean exitoso, String detalle) {
        AuditoriaLog log = new AuditoriaLog();
        log.setUsuarioId(usuarioId);
        log.setUsuarioIdentificador(usuarioIdentificador);
        log.setRol(rol);
        log.setMetodoHttp(metodoHttp);
        log.setEndpoint(endpoint);
        log.setAccion(accion);
        log.setIpOrigen(ipOrigen);
        log.setResultadoHttp(resultadoHttp);
        log.setExitoso(exitoso);
        log.setDetalle(detalle);
        auditoriaLogRepository.save(log);
    }

    /** Para eventos que no pasan por el filtro HTTP genérico (ej. intentos de login antes de emitir el JWT). */
    public void registrarEventoActual(String accion, Long usuarioId, String usuarioIdentificador, String rol,
                                       boolean exitoso, Integer resultadoHttp, String detalle) {
        HttpServletRequest request = obtenerPeticionActual();
        String endpoint = request != null ? request.getMethod() + " " + request.getRequestURI() : accion;
        String metodo = request != null ? request.getMethod() : "N/A";
        String ip = request != null ? obtenerIp(request) : "desconocida";

        registrar(usuarioId, usuarioIdentificador, rol, metodo, endpoint, accion, ip, resultadoHttp, exitoso, detalle);
    }

    public Page<AuditoriaLogDTO> listar(Pageable pageable) {
        return auditoriaLogRepository.findAllByOrderByCreadoEnDesc(pageable).map(this::aDto);
    }

    public Page<AuditoriaLogDTO> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return auditoriaLogRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId, pageable).map(this::aDto);
    }

    public static String obtenerIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private HttpServletRequest obtenerPeticionActual() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private AuditoriaLogDTO aDto(AuditoriaLog log) {
        return new AuditoriaLogDTO(
                log.getId(), log.getUsuarioId(), log.getUsuarioIdentificador(), log.getRol(),
                log.getMetodoHttp(), log.getEndpoint(), log.getAccion(), log.getIpOrigen(),
                log.getResultadoHttp(), log.getExitoso(), log.getDetalle(), log.getCreadoEn()
        );
    }
}
