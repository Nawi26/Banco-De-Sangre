package pe.edu.utp.hlev.bancosangre.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** RBAC (RF-02): sólo el Médico Solicitante (registra solicitudes transfusionales, RF-11). */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("hasAuthority('Médico Solicitante')")
public @interface SoloMedicoSolicitante {
}
