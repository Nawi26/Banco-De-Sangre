package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.ProtocoloClinicoDTO;
import pe.edu.utp.hlev.bancosangre.dto.PublicarProtocoloRequest;
import pe.edu.utp.hlev.bancosangre.model.ProtocoloClinico;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.ProtocoloClinicoRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.util.List;

/**
 * RF-49: control de versiones de los protocolos y guías clínicas vigentes utilizados
 * para validar las solicitudes. Publicar una nueva versión de un protocolo existente
 * retira automáticamente la vigencia de las versiones anteriores del mismo nombre.
 */
@Service
public class ProtocoloClinicoService {

    private final ProtocoloClinicoRepository protocoloClinicoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProtocoloClinicoService(ProtocoloClinicoRepository protocoloClinicoRepository, UsuarioRepository usuarioRepository) {
        this.protocoloClinicoRepository = protocoloClinicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ProtocoloClinicoDTO> listarTodos() {
        return protocoloClinicoRepository.listarTodosConDetalle().stream().map(ProtocoloClinicoDTO::from).toList();
    }

    public List<ProtocoloClinicoDTO> listarVigentes() {
        return protocoloClinicoRepository.listarVigentes().stream().map(ProtocoloClinicoDTO::from).toList();
    }

    @Transactional
    public ProtocoloClinicoDTO publicar(PublicarProtocoloRequest request, Long usuarioId) {
        List<ProtocoloClinico> versionesVigentes = protocoloClinicoRepository.findByNombreAndVigenteTrue(request.nombre());
        versionesVigentes.forEach(v -> v.setVigente(false));
        protocoloClinicoRepository.saveAll(versionesVigentes);

        Usuario publicadoPor = usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null;

        ProtocoloClinico protocolo = new ProtocoloClinico();
        protocolo.setNombre(request.nombre());
        protocolo.setVersion(request.version());
        protocolo.setContenidoUrl(request.contenidoUrl());
        protocolo.setNotasCambio(request.notasCambio());
        protocolo.setVigente(true);
        protocolo.setPublicadoPor(publicadoPor);
        protocoloClinicoRepository.save(protocolo);

        return ProtocoloClinicoDTO.from(protocolo);
    }
}
