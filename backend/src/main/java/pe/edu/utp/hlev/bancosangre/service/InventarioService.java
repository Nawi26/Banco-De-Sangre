package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.InventarioItemDTO;
import pe.edu.utp.hlev.bancosangre.dto.RedBusquedaDTO;
import pe.edu.utp.hlev.bancosangre.dto.ResumenExistenciasDTO;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioService {

    private static final List<String> ESTADOS_ACTIVOS = List.of("DISPONIBLE", "FRACCIONADO");

    private final HemocomponenteRepository hemocomponenteRepository;

    public InventarioService(HemocomponenteRepository hemocomponenteRepository) {
        this.hemocomponenteRepository = hemocomponenteRepository;
    }

    // Algoritmo FEFO: las unidades activas ordenadas por fecha de vencimiento más próxima primero
    public List<InventarioItemDTO> listarInventarioActivo() {
        return hemocomponenteRepository.findByEstadoInOrderByFechaVencimientoAsc(ESTADOS_ACTIVOS)
                .stream()
                .map(InventarioItemDTO::from)
                .toList();
    }

    public List<ResumenExistenciasDTO> resumenPorGrupoSanguineo() {
        return hemocomponenteRepository.resumenPorGrupoSanguineo(ESTADOS_ACTIVOS);
    }

    public RedBusquedaDTO buscarPorCodigo(String codigo) {
        Hemocomponente hemocomponente = hemocomponenteRepository.findByCodigoProductoIsbt(codigo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Unidad no encontrada en la red."));
        return RedBusquedaDTO.from(hemocomponente);
    }
}
