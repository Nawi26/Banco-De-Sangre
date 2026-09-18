package pe.edu.utp.hlev.bancosangre.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RBAC (RF-02): roles con actividad clínico-asistencial directa sobre pacientes
 * (Médico Solicitante, Tecnólogo Médico, Jefe de Banco de Sangre, Administrador).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("hasAnyAuthority('Médico Solicitante', 'Tecnólogo Médico', 'Jefe de Banco de Sangre', 'Administrador')")
public @interface AccesoClinico {
}
