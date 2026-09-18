package pe.edu.utp.hlev.bancosangre.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RBAC (RF-02): personal operativo del banco de sangre
 * (Tecnólogo Médico, Jefe de Banco de Sangre) más Administrador.
 * Para acciones de laboratorio: donantes, donaciones, tamizaje, inventario, despacho.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("hasAnyAuthority('Tecnólogo Médico', 'Jefe de Banco de Sangre', 'Administrador')")
public @interface SoloPersonalBancoSangre {
}
