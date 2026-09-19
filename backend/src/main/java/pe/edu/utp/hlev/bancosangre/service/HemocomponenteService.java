package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.edu.utp.hlev.bancosangre.dto.HemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.IsbtEtiquetaDTO;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;

// RF-05: consulta de hemocomponentes y generación/lectura de la información de sus etiquetas ISBT 128.
@Service
public class HemocomponenteService {

    private final HemocomponenteRepository hemocomponenteRepository;

    public HemocomponenteService(HemocomponenteRepository hemocomponenteRepository) {
        this.hemocomponenteRepository = hemocomponenteRepository;
    }

    public HemocomponenteDTO obtener(Long id) {
        return HemocomponenteDTO.from(buscarPorId(id));
    }

    public IsbtEtiquetaDTO obtenerEtiqueta(Long id) {
        return construirEtiqueta(buscarPorId(id));
    }

    /** RF-05: "lectura" de la unidad a partir del código escaneado (lineal o extraído del payload 2D). */
    public IsbtEtiquetaDTO leerCodigo(String codigo) {
        Hemocomponente hemocomponente = hemocomponenteRepository.buscarPorCodigoConDonacion(codigo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró una unidad con ese código ISBT 128."));
        return construirEtiqueta(hemocomponente);
    }

    private Hemocomponente buscarPorId(Long id) {
        return hemocomponenteRepository.buscarPorIdConDonacion(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hemocomponente no encontrado."));
    }

    private IsbtEtiquetaDTO construirEtiqueta(Hemocomponente h) {
        String din = h.getDonacion() != null ? h.getDonacion().getDinIsbt128() : null;
        String grupoAboRh = h.getGrupoAbo() + h.getFactorRh();
        String payload2D = String.join("|",
                din != null ? din : "",
                h.getCodigoProductoIsbt(),
                grupoAboRh,
                h.getFechaVencimiento() != null ? h.getFechaVencimiento().toLocalDate().toString() : ""
        );

        return new IsbtEtiquetaDTO(
                h.getCodigoProductoIsbt(),
                payload2D,
                din,
                h.getCodigoProductoIsbt(),
                grupoAboRh,
                h.getFechaVencimiento() != null ? h.getFechaVencimiento().toLocalDate() : null
        );
    }
}
