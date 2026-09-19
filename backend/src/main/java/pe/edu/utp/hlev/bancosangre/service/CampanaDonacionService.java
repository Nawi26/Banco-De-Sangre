package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CampanaDonacionDTO;
import pe.edu.utp.hlev.bancosangre.dto.CrearCampanaRequest;
import pe.edu.utp.hlev.bancosangre.model.CampanaDonacion;
import pe.edu.utp.hlev.bancosangre.repository.CampanaDonacionRepository;

import java.util.List;

// RF-23/RF-35: campañas externas de donación (metas y resultados de captación).
@Service
public class CampanaDonacionService {

    private final CampanaDonacionRepository campanaDonacionRepository;

    public CampanaDonacionService(CampanaDonacionRepository campanaDonacionRepository) {
        this.campanaDonacionRepository = campanaDonacionRepository;
    }

    public List<CampanaDonacionDTO> listar() {
        return campanaDonacionRepository.findAllByOrderByFechaInicioDesc().stream()
                .map(CampanaDonacionDTO::from).toList();
    }

    @Transactional
    public CampanaDonacionDTO crear(CrearCampanaRequest request) {
        CampanaDonacion campana = new CampanaDonacion();
        campana.setNombre(request.nombre());
        campana.setInstitucion(request.institucion());
        campana.setTipo(request.tipo());
        campana.setFechaInicio(request.fechaInicio());
        campana.setFechaFin(request.fechaFin());
        campana.setMetaUnidades(request.metaUnidades());
        campana.setUnidadesLogradas(0);
        campana.setEstado("ACTIVA");

        return CampanaDonacionDTO.from(campanaDonacionRepository.save(campana));
    }

    @Transactional
    public void incrementarUnidadesLogradas(CampanaDonacion campana) {
        campana.setUnidadesLogradas(campana.getUnidadesLogradas() + 1);
        campanaDonacionRepository.save(campana);
    }
}
