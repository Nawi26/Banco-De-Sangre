package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CrearTurnoRequest;
import pe.edu.utp.hlev.bancosangre.dto.TurnoPersonalDTO;
import pe.edu.utp.hlev.bancosangre.model.TurnoPersonal;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.TurnoPersonalRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;

/** RF-42: programación y visualización de turnos del personal técnico del banco de sangre. */
@Service
public class TurnoPersonalService {

    private final TurnoPersonalRepository turnoPersonalRepository;
    private final UsuarioRepository usuarioRepository;

    public TurnoPersonalService(TurnoPersonalRepository turnoPersonalRepository, UsuarioRepository usuarioRepository) {
        this.turnoPersonalRepository = turnoPersonalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<TurnoPersonalDTO> listar() {
        return turnoPersonalRepository.listarTodosConDetalle().stream().map(TurnoPersonalDTO::from).toList();
    }

    @Transactional
    public TurnoPersonalDTO programar(CrearTurnoRequest request) {
        if (!request.fechaFin().isAfter(request.fechaInicio())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha de fin del turno debe ser posterior a la de inicio.");
        }
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        TurnoPersonal turno = new TurnoPersonal();
        turno.setUsuario(usuario);
        turno.setFechaInicio(request.fechaInicio());
        turno.setFechaFin(request.fechaFin());
        turno.setTipoTurno(request.tipoTurno());
        turno.setObservaciones(request.observaciones());
        turnoPersonalRepository.save(turno);

        return TurnoPersonalDTO.from(turno);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!turnoPersonalRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado.");
        }
        turnoPersonalRepository.deleteById(id);
    }
}
