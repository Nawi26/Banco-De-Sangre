package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarTemperaturaRequest;
import pe.edu.utp.hlev.bancosangre.dto.RegistroTemperaturaDTO;
import pe.edu.utp.hlev.bancosangre.model.CamaraAlmacenamiento;
import pe.edu.utp.hlev.bancosangre.model.RegistroTemperatura;
import pe.edu.utp.hlev.bancosangre.repository.CamaraAlmacenamientoRepository;
import pe.edu.utp.hlev.bancosangre.repository.RegistroTemperaturaRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-24: registra la temperatura de una cámara (vía integración con sensores u
 * otro medio) y detecta automáticamente las desviaciones fuera del rango seguro.
 */
@Service
public class TemperaturaService {

    private final RegistroTemperaturaRepository registroTemperaturaRepository;
    private final CamaraAlmacenamientoRepository camaraAlmacenamientoRepository;
    private final UsuarioRepository usuarioRepository;

    public TemperaturaService(RegistroTemperaturaRepository registroTemperaturaRepository,
                               CamaraAlmacenamientoRepository camaraAlmacenamientoRepository,
                               UsuarioRepository usuarioRepository) {
        this.registroTemperaturaRepository = registroTemperaturaRepository;
        this.camaraAlmacenamientoRepository = camaraAlmacenamientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RegistroTemperaturaDTO registrar(Long camaraId, RegistrarTemperaturaRequest request, Long usuarioId) {
        CamaraAlmacenamiento camara = camaraAlmacenamientoRepository.findById(camaraId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cámara no encontrada."));

        boolean dentroDeRango = request.temperatura().compareTo(camara.getTemperaturaMinima()) >= 0
                && request.temperatura().compareTo(camara.getTemperaturaMaxima()) <= 0;

        RegistroTemperatura registro = new RegistroTemperatura();
        registro.setCamara(camara);
        registro.setTemperatura(request.temperatura());
        registro.setDentroDeRango(dentroDeRango);
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(registro::setUsuario);
        }

        return RegistroTemperaturaDTO.from(registroTemperaturaRepository.save(registro));
    }

    public List<RegistroTemperaturaDTO> listarPorCamara(Long camaraId) {
        return registroTemperaturaRepository.findByCamaraIdOrderByRegistradoEnDesc(camaraId).stream()
                .map(RegistroTemperaturaDTO::from).toList();
    }

    // RF-14: alertas visuales de desviación de temperatura en las últimas `horas` horas.
    public List<RegistroTemperaturaDTO> alertasRecientes(int horas) {
        return registroTemperaturaRepository.buscarAlertasDesde(LocalDateTime.now().minusHours(horas)).stream()
                .map(RegistroTemperaturaDTO::from).toList();
    }
}
