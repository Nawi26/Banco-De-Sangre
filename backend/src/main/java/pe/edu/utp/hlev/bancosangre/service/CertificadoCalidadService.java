package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CertificadoCalidadDTO;
import pe.edu.utp.hlev.bancosangre.dto.VerificacionCertificadoDTO;
import pe.edu.utp.hlev.bancosangre.model.CertificadoCalidad;
import pe.edu.utp.hlev.bancosangre.model.EstadoHemocomponente;
import pe.edu.utp.hlev.bancosangre.model.Hemocomponente;
import pe.edu.utp.hlev.bancosangre.repository.CertificadoCalidadRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;

import java.util.Set;
import java.util.UUID;

/**
 * RF-41: certificado de calidad por cada unidad procesada, con un código de
 * verificación (QR) que confirma su autenticidad sin necesidad de iniciar sesión.
 */
@Service
public class CertificadoCalidadService {

    // Sólo se certifican unidades que ya superaron el tamizaje serológico (RF-08).
    private static final Set<String> ESTADOS_CERTIFICABLES = Set.of(
            EstadoHemocomponente.DISPONIBLE, EstadoHemocomponente.RESERVADO, EstadoHemocomponente.DESPACHADO
    );

    private final CertificadoCalidadRepository certificadoCalidadRepository;
    private final HemocomponenteRepository hemocomponenteRepository;

    public CertificadoCalidadService(CertificadoCalidadRepository certificadoCalidadRepository,
                                      HemocomponenteRepository hemocomponenteRepository) {
        this.certificadoCalidadRepository = certificadoCalidadRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
    }

    @Transactional
    public CertificadoCalidadDTO obtenerOemitir(Long hemocomponenteId) {
        return certificadoCalidadRepository.findByHemocomponenteId(hemocomponenteId)
                .map(this::aDto)
                .orElseGet(() -> emitir(hemocomponenteId));
    }

    private CertificadoCalidadDTO emitir(Long hemocomponenteId) {
        Hemocomponente hemocomponente = hemocomponenteRepository.findById(hemocomponenteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hemocomponente no encontrado."));

        if (!ESTADOS_CERTIFICABLES.contains(hemocomponente.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Sólo se puede emitir el certificado de calidad de una unidad que superó el tamizaje serológico.");
        }

        CertificadoCalidad certificado = new CertificadoCalidad();
        certificado.setHemocomponente(hemocomponente);
        certificado.setNumeroCertificado("CC-" + hemocomponente.getCodigoProductoIsbt());
        certificado.setCodigoVerificacion(UUID.randomUUID().toString());

        return aDto(certificadoCalidadRepository.save(certificado));
    }

    public VerificacionCertificadoDTO verificar(String codigoVerificacion) {
        return certificadoCalidadRepository.buscarPorCodigoVerificacion(codigoVerificacion)
                .map(c -> {
                    Hemocomponente h = c.getHemocomponente();
                    String din = h.getDonacion() != null ? h.getDonacion().getDinIsbt128() : null;
                    return new VerificacionCertificadoDTO(
                            true, din, h.getCodigoProductoIsbt(), h.getTipoHemocomponente(),
                            h.getGrupoAbo() + h.getFactorRh(), h.getFechaVencimiento(), h.getEstado(), c.getEmitidoEn()
                    );
                })
                .orElseGet(VerificacionCertificadoDTO::invalido);
    }

    private CertificadoCalidadDTO aDto(CertificadoCalidad c) {
        return new CertificadoCalidadDTO(
                c.getHemocomponente().getId(), c.getHemocomponente().getCodigoProductoIsbt(),
                c.getNumeroCertificado(), c.getCodigoVerificacion(), c.getEmitidoEn()
        );
    }
}
