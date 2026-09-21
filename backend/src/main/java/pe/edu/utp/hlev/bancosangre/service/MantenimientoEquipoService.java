package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.MantenimientoEquipoDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarMantenimientoRequest;
import pe.edu.utp.hlev.bancosangre.model.MantenimientoEquipo;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.MantenimientoEquipoRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;
import java.util.Set;

/** RF-47: mantenimiento preventivo y calibración de equipos críticos del banco de sangre. */
@Service
public class MantenimientoEquipoService {

    private static final Set<String> TIPOS_EQUIPO = Set.of("CAMARA_REFRIGERACION", "CONGELADORA", "CENTRIFUGA", "SELLADORA", "OTRO");
    private static final Set<String> TIPOS_MANTENIMIENTO = Set.of("PREVENTIVO", "CALIBRACION", "CORRECTIVO");

    private final MantenimientoEquipoRepository mantenimientoEquipoRepository;
    private final UsuarioRepository usuarioRepository;

    public MantenimientoEquipoService(MantenimientoEquipoRepository mantenimientoEquipoRepository, UsuarioRepository usuarioRepository) {
        this.mantenimientoEquipoRepository = mantenimientoEquipoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MantenimientoEquipoDTO> listar() {
        return mantenimientoEquipoRepository.listarTodosConDetalle().stream().map(MantenimientoEquipoDTO::from).toList();
    }

    @Transactional
    public MantenimientoEquipoDTO registrar(RegistrarMantenimientoRequest request, Long usuarioId) {
        String tipoEquipo = validar(request.tipoEquipo(), TIPOS_EQUIPO, "tipo de equipo");
        String tipoMantenimiento = validar(request.tipoMantenimiento(), TIPOS_MANTENIMIENTO, "tipo de mantenimiento");
        Usuario responsable = usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null;

        MantenimientoEquipo mantenimiento = new MantenimientoEquipo();
        mantenimiento.setNombreEquipo(request.nombreEquipo());
        mantenimiento.setTipoEquipo(tipoEquipo);
        mantenimiento.setTipoMantenimiento(tipoMantenimiento);
        mantenimiento.setFechaRealizado(request.fechaRealizado());
        mantenimiento.setFechaProximoVencimiento(request.fechaProximoVencimiento());
        mantenimiento.setResponsable(responsable);
        mantenimiento.setObservaciones(request.observaciones());
        mantenimientoEquipoRepository.save(mantenimiento);

        return MantenimientoEquipoDTO.from(mantenimiento);
    }

    private String validar(String valor, Set<String> validos, String etiqueta) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase();
        if (!validos.contains(normalizado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Valor de " + etiqueta + " no reconocido: " + valor);
        }
        return normalizado;
    }
}
