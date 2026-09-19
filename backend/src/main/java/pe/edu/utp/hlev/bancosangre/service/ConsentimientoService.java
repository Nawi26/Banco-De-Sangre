package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.ConsentimientoInformadoDTO;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarConsentimientoRequest;
import pe.edu.utp.hlev.bancosangre.model.ConsentimientoInformado;
import pe.edu.utp.hlev.bancosangre.model.Donacion;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.repository.ConsentimientoInformadoRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

// RF-38: consentimiento informado digital del donante previo a la extracción.
@Service
public class ConsentimientoService {

    private final ConsentimientoInformadoRepository consentimientoInformadoRepository;
    private final DonanteRepository donanteRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsentimientoService(ConsentimientoInformadoRepository consentimientoInformadoRepository,
                                  DonanteRepository donanteRepository, UsuarioRepository usuarioRepository) {
        this.consentimientoInformadoRepository = consentimientoInformadoRepository;
        this.donanteRepository = donanteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ConsentimientoInformadoDTO registrar(Long donanteId, RegistrarConsentimientoRequest request,
                                                 Long usuarioId, String ipOrigen) {
        Donante donante = donanteRepository.findById(donanteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donante no encontrado."));

        if (!request.aceptado()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El donante debe aceptar el consentimiento informado para continuar con la extracción.");
        }

        ConsentimientoInformado consentimiento = new ConsentimientoInformado();
        consentimiento.setDonante(donante);
        consentimiento.setTipoValidacion(request.tipoValidacion());
        consentimiento.setEvidenciaValidacion(request.evidenciaValidacion());
        consentimiento.setAceptado(true);
        consentimiento.setIpOrigen(ipOrigen);
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(consentimiento::setUsuarioRegistro);
        }

        return ConsentimientoInformadoDTO.from(consentimientoInformadoRepository.save(consentimiento));
    }

    /** Consentimiento vigente (aún no asociado a una donación concreta) para permitir la extracción. */
    public boolean tieneConsentimientoVigente(Long donanteId) {
        return consentimientoInformadoRepository
                .findFirstByDonanteIdAndDonacionIsNullOrderByCreadoEnDesc(donanteId)
                .map(ConsentimientoInformado::getAceptado)
                .orElse(false);
    }

    /** Marca el consentimiento vigente como consumido por esta donación concreta. */
    @Transactional
    public void vincularADonacion(Long donanteId, Donacion donacion) {
        consentimientoInformadoRepository
                .findFirstByDonanteIdAndDonacionIsNullOrderByCreadoEnDesc(donanteId)
                .ifPresent(consentimiento -> {
                    consentimiento.setDonacion(donacion);
                    consentimientoInformadoRepository.save(consentimiento);
                });
    }
}
