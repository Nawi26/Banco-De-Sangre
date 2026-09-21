package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CamaraAlmacenamientoDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearCamaraRequest;
import pe.edu.utp.hlev.bancosangre.model.CamaraAlmacenamiento;
import pe.edu.utp.hlev.bancosangre.repository.CamaraAlmacenamientoRepository;

import java.util.List;

// RF-09/RF-24: cámaras de refrigeración/congelación del banco de sangre.
@Service
public class CamaraAlmacenamientoService {

    private final CamaraAlmacenamientoRepository camaraAlmacenamientoRepository;

    public CamaraAlmacenamientoService(CamaraAlmacenamientoRepository camaraAlmacenamientoRepository) {
        this.camaraAlmacenamientoRepository = camaraAlmacenamientoRepository;
    }

    public List<CamaraAlmacenamientoDTO> listar() {
        return camaraAlmacenamientoRepository.findAllByOrderByNombreAsc().stream()
                .map(CamaraAlmacenamientoDTO::from).toList();
    }

    @Transactional
    public CamaraAlmacenamientoDTO crear(CrearCamaraRequest request) {
        CamaraAlmacenamiento camara = new CamaraAlmacenamiento();
        camara.setNombre(request.nombre());
        camara.setTipo(request.tipo());
        camara.setUbicacion(request.ubicacion());
        camara.setTemperaturaMinima(request.temperaturaMinima());
        camara.setTemperaturaMaxima(request.temperaturaMaxima());
        camara.setActiva(true);

        return CamaraAlmacenamientoDTO.from(camaraAlmacenamientoRepository.save(camara));
    }
}
