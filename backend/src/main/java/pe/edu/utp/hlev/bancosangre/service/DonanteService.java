package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonanteRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonanteDTO;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonanteService {

    private final DonanteRepository donanteRepository;

    public DonanteService(DonanteRepository donanteRepository) {
        this.donanteRepository = donanteRepository;
    }

    public List<DonanteDTO> listar() {
        return donanteRepository.findAllByOrderByIdDesc().stream().map(DonanteDTO::from).toList();
    }

    @Transactional
    public DonanteDTO crear(CrearDonanteRequest request) {
        if (donanteRepository.existsByNumDoc(request.numDoc())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un donante registrado con ese número de documento.");
        }

        Donante donante = new Donante();
        donante.setTipoDoc(request.tipoDoc());
        donante.setNumDoc(request.numDoc());
        donante.setNombres(request.nombres());
        donante.setApellidos(request.apellidos());
        donante.setFechaNacimiento(request.fechaNacimiento());
        donante.setSexo(request.sexo());
        donante.setGrupoAbo(request.grupoAbo());
        donante.setFactorRh(request.factorRh());
        donante.setEstadoDiferido(false);
        donante.setEsOcupacionRiesgo(false);

        return DonanteDTO.from(donanteRepository.save(donante));
    }
}
