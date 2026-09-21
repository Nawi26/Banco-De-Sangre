package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.RegistrarTamizajeRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResolverDiscordanciaRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResultadoMarcadorInput;
import pe.edu.utp.hlev.bancosangre.dto.TamizajeSerologicoDTO;
import pe.edu.utp.hlev.bancosangre.model.*;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import pe.edu.utp.hlev.bancosangre.repository.HemocomponenteRepository;
import pe.edu.utp.hlev.bancosangre.repository.TamizajeSerologicoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RF-07: tamizaje serológico de los 7 marcadores infecciosos, con doble digitación
 * ciega (o importación validada desde un LIS).
 * RF-08: bloqueo automático, irreversible e inmediato de toda unidad con resultado
 * reactivo o indeterminado, impidiendo su distribución/despacho.
 */
@Service
public class TamizajeSerologicoService {

    private static final String ORIGEN_DOBLE_DIGITACION = "DOBLE_DIGITACION";
    private static final String ORIGEN_LIS = "LIS";

    private static final String ESTADO_PENDIENTE_SEGUNDA = "PENDIENTE_SEGUNDA_DIGITACION";
    private static final String ESTADO_DISCORDANTE = "DISCORDANTE";
    private static final String ESTADO_CONFIRMADO = "CONFIRMADO";

    // RF-04: un marcador reactivo confirmado difiere al donante de forma permanente;
    // uno indeterminado, de forma temporal hasta poder repetir la prueba.
    private static final int DIAS_DIFERIMIENTO_INDETERMINADO = 90;

    private final TamizajeSerologicoRepository tamizajeSerologicoRepository;
    private final DonacionRepository donacionRepository;
    private final DonanteRepository donanteRepository;
    private final HemocomponenteRepository hemocomponenteRepository;
    private final FirmaElectronicaService firmaElectronicaService;
    private final HemocomponenteEventoService hemocomponenteEventoService;

    public TamizajeSerologicoService(TamizajeSerologicoRepository tamizajeSerologicoRepository,
                                      DonacionRepository donacionRepository, DonanteRepository donanteRepository,
                                      HemocomponenteRepository hemocomponenteRepository,
                                      FirmaElectronicaService firmaElectronicaService,
                                      HemocomponenteEventoService hemocomponenteEventoService) {
        this.tamizajeSerologicoRepository = tamizajeSerologicoRepository;
        this.donacionRepository = donacionRepository;
        this.donanteRepository = donanteRepository;
        this.hemocomponenteRepository = hemocomponenteRepository;
        this.firmaElectronicaService = firmaElectronicaService;
        this.hemocomponenteEventoService = hemocomponenteEventoService;
    }

    public TamizajeSerologicoDTO obtenerPorDonacion(Long donacionId) {
        return TamizajeSerologicoDTO.from(buscarPorDonacion(donacionId));
    }

    @Transactional
    public TamizajeSerologicoDTO primeraDigitacion(Long donacionId, RegistrarTamizajeRequest request, Long usuarioId) {
        if (tamizajeSerologicoRepository.existsByDonacionId(donacionId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un tamizaje registrado para esta donación.");
        }
        Donacion donacion = donacionRepository.findById(donacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donación no encontrada."));
        Usuario usuario1 = firmaElectronicaService.validar(usuarioId, request.claveConfirmacion());

        String origen = request.origen() != null && !request.origen().isBlank() ? request.origen() : ORIGEN_DOBLE_DIGITACION;

        TamizajeSerologico tamizaje = new TamizajeSerologico();
        tamizaje.setDonacion(donacion);
        tamizaje.setOrigen(origen);
        tamizaje.setUsuario1(usuario1);
        tamizaje.setFechaPrimeraDigitacion(LocalDateTime.now());

        for (ResultadoMarcadorInput input : request.resultados()) {
            validarMarcador(input.marcador());
            validarResultado(input.resultado());

            ResultadoMarcador resultado = new ResultadoMarcador();
            resultado.setTamizaje(tamizaje);
            resultado.setMarcador(input.marcador());
            resultado.setResultadoDigitacion1(input.resultado());
            tamizaje.getResultados().add(resultado);
        }

        if (ORIGEN_LIS.equals(origen)) {
            // RF-07: interfaz LIS — resultado ya validado por el instrumento, no requiere doble digitación.
            tamizaje.getResultados().forEach(r -> {
                r.setResultadoDigitacion2(r.getResultadoDigitacion1());
                r.setResultadoFinal(r.getResultadoDigitacion1());
                r.setConcordante(true);
            });
            tamizaje.setFechaSegundaDigitacion(tamizaje.getFechaPrimeraDigitacion());
            tamizaje.setEstado(ESTADO_CONFIRMADO);
            tamizajeSerologicoRepository.save(tamizaje);
            finalizar(tamizaje);
        } else {
            tamizaje.setEstado(ESTADO_PENDIENTE_SEGUNDA);
            tamizajeSerologicoRepository.save(tamizaje);
        }

        return TamizajeSerologicoDTO.from(tamizaje);
    }

    @Transactional
    public TamizajeSerologicoDTO segundaDigitacion(Long donacionId, RegistrarTamizajeRequest request, Long usuarioId) {
        TamizajeSerologico tamizaje = buscarPorDonacion(donacionId);

        if (!ESTADO_PENDIENTE_SEGUNDA.equals(tamizaje.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Este tamizaje no está pendiente de segunda digitación.");
        }

        Usuario usuario2 = firmaElectronicaService.validar(usuarioId, request.claveConfirmacion());
        if (tamizaje.getUsuario1() != null && tamizaje.getUsuario1().getId().equals(usuario2.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La segunda digitación (ciega) debe ser realizada por un responsable distinto al de la primera.");
        }

        Map<String, String> segundaPorMarcador = request.resultados().stream()
                .peek(r -> {
                    validarMarcador(r.marcador());
                    validarResultado(r.resultado());
                })
                .collect(java.util.stream.Collectors.toMap(ResultadoMarcadorInput::marcador, ResultadoMarcadorInput::resultado));

        boolean todoConcordante = true;
        for (ResultadoMarcador resultado : tamizaje.getResultados()) {
            String segundo = segundaPorMarcador.get(resultado.getMarcador());
            if (segundo == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Falta el resultado del marcador " + resultado.getMarcador() + " en la segunda digitación.");
            }
            resultado.setResultadoDigitacion2(segundo);
            boolean concordante = segundo.equals(resultado.getResultadoDigitacion1());
            resultado.setConcordante(concordante);
            if (concordante) {
                resultado.setResultadoFinal(segundo);
            } else {
                todoConcordante = false;
            }
        }

        tamizaje.setUsuario2(usuario2);
        tamizaje.setFechaSegundaDigitacion(LocalDateTime.now());

        if (todoConcordante) {
            tamizaje.setEstado(ESTADO_CONFIRMADO);
            tamizajeSerologicoRepository.save(tamizaje);
            finalizar(tamizaje);
        } else {
            tamizaje.setEstado(ESTADO_DISCORDANTE);
            tamizajeSerologicoRepository.save(tamizaje);
        }

        return TamizajeSerologicoDTO.from(tamizaje);
    }

    /** RF-07: el Jefe de Banco de Sangre dirime los marcadores discordantes entre ambas digitaciones. */
    @Transactional
    public TamizajeSerologicoDTO resolverDiscordancia(Long donacionId, ResolverDiscordanciaRequest request, Long usuarioId) {
        TamizajeSerologico tamizaje = buscarPorDonacion(donacionId);
        if (!ESTADO_DISCORDANTE.equals(tamizaje.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Este tamizaje no tiene marcadores discordantes pendientes de resolución.");
        }
        firmaElectronicaService.validar(usuarioId, request.claveConfirmacion());

        Map<String, String> definitivos = request.resultadosDefinitivos().stream()
                .peek(r -> validarResultado(r.resultado()))
                .collect(java.util.stream.Collectors.toMap(ResultadoMarcadorInput::marcador, ResultadoMarcadorInput::resultado));

        for (ResultadoMarcador resultado : tamizaje.getResultados()) {
            if (Boolean.FALSE.equals(resultado.getConcordante())) {
                String definitivo = definitivos.get(resultado.getMarcador());
                if (definitivo == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Falta el resultado definitivo del marcador discordante " + resultado.getMarcador() + ".");
                }
                resultado.setResultadoFinal(definitivo);
            }
        }

        tamizaje.setEstado(ESTADO_CONFIRMADO);
        tamizajeSerologicoRepository.save(tamizaje);
        finalizar(tamizaje);

        return TamizajeSerologicoDTO.from(tamizaje);
    }

    /** RF-08: bloqueo (o liberación) automático, irreversible e inmediato según el resultado del tamizaje. */
    private void finalizar(TamizajeSerologico tamizaje) {
        boolean hayReactivo = tamizaje.getResultados().stream()
                .anyMatch(r -> ResultadoSerologico.REACTIVO.name().equals(r.getResultadoFinal()));
        boolean hayIndeterminado = tamizaje.getResultados().stream()
                .anyMatch(r -> ResultadoSerologico.INDETERMINADO.name().equals(r.getResultadoFinal()));

        boolean apto = !hayReactivo && !hayIndeterminado;
        tamizaje.setResultadoGeneral(apto ? ResultadoSerologico.NO_REACTIVO.name()
                : hayReactivo ? ResultadoSerologico.REACTIVO.name() : ResultadoSerologico.INDETERMINADO.name());
        tamizajeSerologicoRepository.save(tamizaje);

        Donacion donacion = tamizaje.getDonacion();
        donacion.setTamizajeAprobado(apto);
        donacionRepository.save(donacion);

        // Toda unidad fraccionada de esta donación pasa de CUARENTENA a DISPONIBLE o BLOQUEADO.
        // El bloqueo es irreversible: ningún otro flujo del sistema revierte el estado BLOQUEADO.
        List<Hemocomponente> hemocomponentes = hemocomponenteRepository.findByDonacionId(donacion.getId());
        Long usuarioResponsable = tamizaje.getUsuario2() != null ? tamizaje.getUsuario2().getId()
                : tamizaje.getUsuario1() != null ? tamizaje.getUsuario1().getId() : null;
        for (Hemocomponente h : hemocomponentes) {
            if (!EstadoHemocomponente.CUARENTENA.equals(h.getEstado())) {
                continue; // no se toca una unidad que ya fue movida por otro proceso (ej. ya bloqueada antes)
            }
            h.setEstado(apto ? EstadoHemocomponente.DISPONIBLE : EstadoHemocomponente.BLOQUEADO);
            hemocomponenteEventoService.registrar(h, apto ? "LIBERACION" : "BLOQUEO",
                    "Tamizaje serológico con resultado " + tamizaje.getResultadoGeneral() + ".", usuarioResponsable);
        }
        hemocomponenteRepository.saveAll(hemocomponentes);

        if (hayReactivo || hayIndeterminado) {
            diferirDonante(donacion.getDonante(), hayReactivo);
        }
    }

    private void diferirDonante(Donante donante, boolean permanente) {
        donante.setApto(false);
        donante.setEstadoDiferido(true);
        if (permanente) {
            donante.setTipoDiferimiento("PERMANENTE");
            donante.setMotivoDiferimiento("Resultado reactivo confirmado en el tamizaje serológico de una donación previa.");
            donante.setDiferidoHasta(null);
        } else {
            donante.setTipoDiferimiento("TEMPORAL");
            donante.setMotivoDiferimiento("Resultado indeterminado en el tamizaje serológico; pendiente de repetir la prueba.");
            donante.setDiferidoHasta(LocalDate.now().plusDays(DIAS_DIFERIMIENTO_INDETERMINADO));
        }
        donanteRepository.save(donante);
    }

    private TamizajeSerologico buscarPorDonacion(Long donacionId) {
        return tamizajeSerologicoRepository.buscarPorDonacionConResultados(donacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se ha registrado un tamizaje para esta donación."));
    }

    private void validarMarcador(String marcador) {
        try {
            MarcadorSerologico.valueOf(marcador);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Marcador serológico no reconocido: " + marcador);
        }
    }

    private void validarResultado(String resultado) {
        try {
            ResultadoSerologico.valueOf(resultado);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Resultado serológico no reconocido: " + resultado);
        }
    }
}
