package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.FraccionarDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.HemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.model.Donacion;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.TipoHemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;

import java.util.List;

/**
 * RF-06: fraccionamiento de la bolsa de sangre total en hemocomponentes,
 * conservando la trazabilidad hacia el DIN matriz (la donación de origen).
 */
@Service
public class FraccionamientoService {

    private final DonacionRepository donacionRepository;
    private final HemocomponenteRepository hemocomponenteRepository;

    public FraccionamientoService(DonacionRepository donacionRepository, HemocomponenteRepository hemocomponenteRepository) {
        this.donacionRepository = donacionRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
    }

    @Transactional
    public List<HemocomponenteDTO> fraccionar(Long donacionId, FraccionarDonacionRequest request) {
        Donacion donacion = donacionRepository.findById(donacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donación no encontrada."));

        return request.tiposHemocomponente().stream()
                .map(tipo -> crearHemocomponente(donacion, parsearTipo(tipo)))
                .map(HemocomponenteDTO::from)
                .toList();
    }

    public List<HemocomponenteDTO> listarPorDonacion(Long donacionId) {
        return hemocomponenteRepository.findByDonacionIdConDonacion(donacionId).stream()
                .map(HemocomponenteDTO::from).toList();
    }

    private Hemocomponente crearHemocomponente(Donacion donacion, TipoHemocomponente tipo) {
        Hemocomponente hemocomponente = new Hemocomponente();
        hemocomponente.setDonacion(donacion);
        hemocomponente.setTipoHemocomponente(tipo.getNombre());
        hemocomponente.setGrupoAbo(donacion.getDonante().getGrupoAbo());
        hemocomponente.setFactorRh(donacion.getDonante().getFactorRh());
        hemocomponente.setVolumenMl(tipo.getVolumenEstimadoMl());
        hemocomponente.setFechaVencimiento(donacion.getFechaExtraccion().plusDays(tipo.getDiasVidaUtil()));
        hemocomponente.setUbicacionFisica("PENDIENTE_ASIGNACION");
        // RF-10: toda unidad recién fraccionada queda en cuarentena hasta que el
        // tamizaje serológico (RF-07/RF-08) determine si puede liberarse o debe bloquearse.
        hemocomponente.setEstado(EstadoHemocomponente.CUARENTENA);
        hemocomponente.setCodigoProductoIsbt(generarCodigoProductoIsbt(donacion, tipo));

        return hemocomponenteRepository.save(hemocomponente);
    }

    private TipoHemocomponente parsearTipo(String tipo) {
        try {
            return TipoHemocomponente.valueOf(tipo);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo de hemocomponente no reconocido: " + tipo);
        }
    }

    // RF-05: código de producto ISBT 128 (simplificado) — DIN matriz + prefijo de producto + secuencial.
    private String generarCodigoProductoIsbt(Donacion donacion, TipoHemocomponente tipo) {
        long secuencial = hemocomponenteRepository.countByDonacionIdAndTipoHemocomponente(donacion.getId(), tipo.getNombre()) + 1;
        return donacion.getDinIsbt128() + tipo.getPrefijoIsbt() + String.format("%02d", secuencial);
    }
}
