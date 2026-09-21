package pe.edu.utp.hlev.bancosangre.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * RF-32: orden médica de solicitud transfusional recibida desde el HIS/SIS hospitalario.
 * Referencia al paciente y al médico por documento de identidad (no por ID interno), ya
 * que el sistema externo no conoce nuestras claves primarias.
 */
public record ImportarOrdenHisSisRequest(
        @NotBlank String codigoOrdenExterna,
        @NotBlank String pacienteNumDoc,
        String pacienteNombres,
        String pacienteApellidos,
        @NotBlank String medicoDni,
        @NotBlank String tipoHemocomponente,
        @NotNull @Min(1) Integer unidadesSolicitadas,
        @NotBlank String prioridad,
        String indicacionClinica,
        @NotBlank String diagnosticoCie10
) {
}
