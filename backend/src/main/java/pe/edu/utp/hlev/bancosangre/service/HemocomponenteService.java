package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.AsignarUbicacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.DescarteHemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.HemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.HemocomponenteEventoDTO;
import pe.edu.utp.hlev.bancosangre.dto.HistorialHemocomponenteDTO;
import pe.edu.utp.hlev.bancosangre.dto.IsbtEtiquetaDTO;
import pe.edu.utp.hlev.bancosangre.model.CamaraAlmacenamiento;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.model.TamizajeSerologico;
import pe.edu.utp.hlev.bancosangre.repository.CamaraAlmacenamientoRepository;
import pe.edu.utp.hlev.bancosangre.repository.DescarteHemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteEventoRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.TamizajeSerologicoRepository;

import java.time.LocalDateTime;
import java.util.List;

// RF-05: consulta de hemocomponentes y generación/lectura de la información de sus etiquetas ISBT 128.
@Service
public class HemocomponenteService {

    private final HemocomponenteRepository hemocomponenteRepository;
    private final CamaraAlmacenamientoRepository camaraAlmacenamientoRepository;
    private final HemocomponenteEventoRepository hemocomponenteEventoRepository;
    private final DescarteHemocomponenteRepository descarteHemocomponenteRepository;
    private final TamizajeSerologicoRepository tamizajeSerologicoRepository;

    public HemocomponenteService(HemocomponenteRepository hemocomponenteRepository,
                                  CamaraAlmacenamientoRepository camaraAlmacenamientoRepository,
                                  HemocomponenteEventoRepository hemocomponenteEventoRepository,
                                  DescarteHemocomponenteRepository descarteHemocomponenteRepository,
                                  TamizajeSerologicoRepository tamizajeSerologicoRepository) {
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.camaraAlmacenamientoRepository = camaraAlmacenamientoRepository;
        this.hemocomponenteEventoRepository = hemocomponenteEventoRepository;
        this.descarteHemocomponenteRepository = descarteHemocomponenteRepository;
        this.tamizajeSerologicoRepository = tamizajeSerologicoRepository;
    }

    public HemocomponenteDTO obtener(Long id) {
        return HemocomponenteDTO.from(buscarPorId(id));
    }

    // RF-09: asigna físicamente la unidad a una cámara de refrigeración/congelación.
    @Transactional
    public HemocomponenteDTO asignarUbicacion(Long id, AsignarUbicacionRequest request) {
        Hemocomponente hemocomponente = buscarPorId(id);
        CamaraAlmacenamiento camara = camaraAlmacenamientoRepository.findById(request.camaraId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cámara no encontrada."));

        hemocomponente.setCamara(camara);
        hemocomponente.setUbicacionFisica(camara.getNombre());

        return HemocomponenteDTO.from(hemocomponenteRepository.save(hemocomponente));
    }

    public IsbtEtiquetaDTO obtenerEtiqueta(Long id) {
        return construirEtiqueta(buscarPorId(id));
    }

    // RF-30: historial completo del ciclo de vida, desde la donación hasta su destino final.
    public HistorialHemocomponenteDTO obtenerHistorial(Long id) {
        Hemocomponente h = hemocomponenteRepository.buscarPorIdParaHistorial(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hemocomponente no encontrado."));

        String donanteNombreCompleto = h.getDonacion() != null && h.getDonacion().getDonante() != null
                ? h.getDonacion().getDonante().getNombres() + " " + h.getDonacion().getDonante().getApellidos()
                : null;
        LocalDateTime fechaExtraccion = h.getDonacion() != null ? h.getDonacion().getFechaExtraccion() : null;

        String resultadoTamizaje = h.getDonacion() != null
                ? tamizajeSerologicoRepository.findByDonacionId(h.getDonacion().getId())
                        .map(TamizajeSerologico::getResultadoGeneral)
                        .orElse(null)
                : null;

        DescarteHemocomponenteDTO descarte = descarteHemocomponenteRepository.findByHemocomponenteId(id)
                .map(DescarteHemocomponenteDTO::from)
                .orElse(null);

        List<HemocomponenteEventoDTO> eventos = hemocomponenteEventoRepository.findByHemocomponenteIdOrderByFechaAsc(id)
                .stream().map(HemocomponenteEventoDTO::from).toList();

        return new HistorialHemocomponenteDTO(HemocomponenteDTO.from(h), donanteNombreCompleto, fechaExtraccion,
                resultadoTamizaje, descarte, eventos);
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
