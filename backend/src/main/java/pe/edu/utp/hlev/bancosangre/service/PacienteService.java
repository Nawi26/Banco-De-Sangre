package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearPacienteRequest;
import pe.edu.utp.hlev.bancosangre.dto.PacienteDTO;
import pe.edu.utp.hlev.bancosangre.model.Paciente;
import pe.edu.utp.hlev.bancosangre.repository.PacienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<PacienteDTO> listar() {
        return pacienteRepository.findAllByOrderByIdDesc().stream().map(PacienteDTO::from).toList();
    }

    @Transactional
    public PacienteDTO crear(CrearPacienteRequest request) {
        if (pacienteRepository.existsByNumDoc(request.numDoc())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un paciente registrado con ese número de documento.");
        }

        Paciente paciente = new Paciente();
        paciente.setTipoDoc(request.tipoDoc());
        paciente.setNumDoc(request.numDoc());
        paciente.setNombres(request.nombres());
        paciente.setApellidos(request.apellidos());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setSexo(request.sexo());
        paciente.setGrupoAbo(request.grupoAbo());
        paciente.setFactorRh(request.factorRh());

        return PacienteDTO.from(pacienteRepository.save(paciente));
    }
}
