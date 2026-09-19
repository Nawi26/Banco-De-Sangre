package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.CuestionarioTriajeRequest;
import pe.edu.utp.hlev.bancosangre.dto.ResultadoTriajeDTO;
import pe.edu.utp.hlev.bancosangre.model.CuestionarioTriaje;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.CuestionarioTriajeRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * RF-03/RF-04: determina automáticamente la aptitud del donante y aplica el
 * diferimiento (temporal o permanente) de forma inmediata, según el cuestionario
 * de triaje clínico y causales normativas de referencia (PRONAHEBAS/MINSA).
 * Los umbrales son una referencia simplificada para el sistema; el Jefe de
 * Banco de Sangre puede ajustar la parametrización clínica en una fase posterior.
 */
@Service
public class TriajeService {

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 65;
    private static final BigDecimal PESO_MINIMO_KG = BigDecimal.valueOf(50);
    private static final BigDecimal HEMOGLOBINA_MINIMA_MUJER = BigDecimal.valueOf(12.5);
    private static final BigDecimal HEMOGLOBINA_MINIMA_HOMBRE = BigDecimal.valueOf(13.0);
    private static final int PRESION_SISTOLICA_MIN = 90;
    private static final int PRESION_SISTOLICA_MAX = 180;
    private static final int PRESION_DIASTOLICA_MIN = 60;
    private static final int PRESION_DIASTOLICA_MAX = 100;
    private static final int INTERVALO_MINIMO_ENTRE_DONACIONES_DIAS = 56;

    public static final String DIFERIMIENTO_NINGUNO = "NINGUNO";
    public static final String DIFERIMIENTO_TEMPORAL = "TEMPORAL";
    public static final String DIFERIMIENTO_PERMANENTE = "PERMANENTE";

    private final DonanteRepository donanteRepository;
    private final CuestionarioTriajeRepository cuestionarioTriajeRepository;
    private final UsuarioRepository usuarioRepository;

    public TriajeService(DonanteRepository donanteRepository, CuestionarioTriajeRepository cuestionarioTriajeRepository,
                          UsuarioRepository usuarioRepository) {
        this.donanteRepository = donanteRepository;
        this.cuestionarioTriajeRepository = cuestionarioTriajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ResultadoTriajeDTO evaluar(Long donanteId, CuestionarioTriajeRequest request, Long evaluadorId) {
        Donante donante = donanteRepository.findById(donanteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donante no encontrado."));

        ResultadoTriajeDTO resultado = calcularResultado(donante, request);

        // Actualiza la ficha clínica integral del donante (RF-03).
        donante.setPesoKg(request.pesoKg());
        donante.setTallaCm(request.tallaCm());
        donante.setPresionSistolica(request.presionSistolica());
        donante.setPresionDiastolica(request.presionDiastolica());
        donante.setPulso(request.pulso());
        donante.setHemoglobina(request.hemoglobina());
        donante.setApto(resultado.apto());
        donante.setTipoDiferimiento(resultado.tipoDiferimiento());
        donante.setMotivoDiferimiento(resultado.motivo());
        donante.setDiferidoHasta(resultado.diferidoHasta());
        donante.setEstadoDiferido(!resultado.apto());
        donanteRepository.save(donante);

        CuestionarioTriaje cuestionario = new CuestionarioTriaje();
        cuestionario.setDonante(donante);
        cuestionario.setAntecedenteIts(request.antecedenteIts());
        cuestionario.setAntecedenteUsoDrogas(request.antecedenteUsoDrogas());
        cuestionario.setTatuajeOPerforacionReciente(request.tatuajeOPerforacionReciente());
        cuestionario.setEmbarazoOPartoReciente(request.embarazoOPartoReciente());
        cuestionario.setViajeZonaEndemica(request.viajeZonaEndemica());
        cuestionario.setOtrosAntecedentes(request.otrosAntecedentes());
        cuestionario.setResultadoApto(resultado.apto());
        cuestionario.setResultadoTipoDiferimiento(resultado.tipoDiferimiento());
        cuestionario.setResultadoMotivo(resultado.motivo());
        cuestionario.setResultadoDiferidoHasta(resultado.diferidoHasta());
        if (evaluadorId != null) {
            Usuario evaluador = usuarioRepository.findById(evaluadorId).orElse(null);
            cuestionario.setEvaluador(evaluador);
        }
        cuestionarioTriajeRepository.save(cuestionario);

        return resultado;
    }

    private ResultadoTriajeDTO calcularResultado(Donante donante, CuestionarioTriajeRequest request) {
        LocalDate hoy = LocalDate.now();

        // --- Causales de diferimiento PERMANENTE ---
        if (request.antecedenteIts() || request.antecedenteUsoDrogas()) {
            return new ResultadoTriajeDTO(false, DIFERIMIENTO_PERMANENTE,
                    "Antecedente de infección de transmisión sexual o uso de drogas endovenosas.", null);
        }

        if (donante.getFechaNacimiento() != null) {
            int edad = Period.between(donante.getFechaNacimiento(), hoy).getYears();
            if (edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
                return new ResultadoTriajeDTO(false, DIFERIMIENTO_PERMANENTE,
                        "Edad fuera del rango permitido para donar (" + EDAD_MINIMA + "-" + EDAD_MAXIMA + " años).", null);
            }
        }

        // --- Causales de diferimiento TEMPORAL ---
        if (donante.getFechaUltimaDonacion() != null &&
                donante.getFechaUltimaDonacion().plusDays(INTERVALO_MINIMO_ENTRE_DONACIONES_DIAS).isAfter(hoy)) {
            LocalDate hasta = donante.getFechaUltimaDonacion().plusDays(INTERVALO_MINIMO_ENTRE_DONACIONES_DIAS);
            return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                    "No ha transcurrido el intervalo mínimo entre donaciones (" + INTERVALO_MINIMO_ENTRE_DONACIONES_DIAS + " días).", hasta);
        }

        if (request.pesoKg() != null && request.pesoKg().compareTo(PESO_MINIMO_KG) < 0) {
            return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                    "Peso inferior al mínimo permitido (" + PESO_MINIMO_KG + " kg).", null);
        }

        if (request.hemoglobina() != null) {
            boolean esMujer = "F".equalsIgnoreCase(donante.getSexo());
            BigDecimal minimoHemoglobina = esMujer ? HEMOGLOBINA_MINIMA_MUJER : HEMOGLOBINA_MINIMA_HOMBRE;
            if (request.hemoglobina().compareTo(minimoHemoglobina) < 0) {
                return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                        "Hemoglobina por debajo del mínimo permitido (" + minimoHemoglobina + " g/dL).", hoy.plusDays(30));
            }
        }

        if (request.presionSistolica() != null && request.presionDiastolica() != null) {
            boolean fueraDeRango = request.presionSistolica() < PRESION_SISTOLICA_MIN
                    || request.presionSistolica() > PRESION_SISTOLICA_MAX
                    || request.presionDiastolica() < PRESION_DIASTOLICA_MIN
                    || request.presionDiastolica() > PRESION_DIASTOLICA_MAX;
            if (fueraDeRango) {
                return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                        "Presión arterial fuera del rango permitido en el momento de la evaluación.", hoy.plusDays(1));
            }
        }

        if (request.embarazoOPartoReciente()) {
            return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                    "Embarazo o parto reciente.", hoy.plusMonths(6));
        }

        if (request.tatuajeOPerforacionReciente()) {
            return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                    "Tatuaje o perforación reciente (riesgo de exposición percutánea).", hoy.plusMonths(4));
        }

        if (request.viajeZonaEndemica() || Boolean.TRUE.equals(donante.getEsOcupacionRiesgo())) {
            return new ResultadoTriajeDTO(false, DIFERIMIENTO_TEMPORAL,
                    "Viaje a zona endémica o exposición ocupacional de riesgo.", hoy.plusMonths(12));
        }

        return new ResultadoTriajeDTO(true, DIFERIMIENTO_NINGUNO, null, null);
    }
}
