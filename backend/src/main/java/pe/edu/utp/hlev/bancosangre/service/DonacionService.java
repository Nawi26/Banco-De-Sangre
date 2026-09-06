package pe.edu.utp.hlev.bancosangre.service;

import pe.edu.utp.hlev.bancosangre.dto.CrearDonacionRequest;
import pe.edu.utp.hlev.bancosangre.dto.DonacionDTO;
import pe.edu.utp.hlev.bancosangre.model.Donacion;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final DonanteRepository donanteRepository;

    public DonacionService(DonacionRepository donacionRepository, DonanteRepository donanteRepository) {
        this.donacionRepository = donacionRepository;
        this.donanteRepository = donanteRepository;
    }

    public List<DonacionDTO> listar() {
        return donacionRepository.listarTodasConDonante().stream().map(DonacionDTO::from).toList();
    }

    public long contarDesde(LocalDateTime desde) {
        return donacionRepository.countByFechaExtraccionAfter(desde);
    }

    @Transactional
    public DonacionDTO crear(CrearDonacionRequest request) {
        Donante donante = donanteRepository.findById(request.donanteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donante no encontrado."));

        Donacion donacion = new Donacion();
        donacion.setDinIsbt128(generarDinIsbt128());
        donacion.setDonante(donante);
        donacion.setFechaExtraccion(LocalDateTime.now());
        donacion.setVolumenMl(request.volumenMl());
        donacion.setTipoDonacion(request.tipoDonacion() != null && !request.tipoDonacion().isBlank() ? request.tipoDonacion() : "VOLUNTARIA");
        donacion.setTamizajeAprobado(false);

        Donacion guardada = donacionRepository.save(donacion);
        // Recargar con el donante ya inicializado para armar el DTO sin problemas de lazy-loading
        return DonacionDTO.from(guardada);
    }

    private String generarDinIsbt128() {
        long siguiente = donacionRepository.count() + 1;
        return "W" + String.format("%011d", siguiente);
    }
}
