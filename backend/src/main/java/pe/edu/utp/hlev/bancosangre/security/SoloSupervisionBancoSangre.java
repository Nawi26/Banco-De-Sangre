package pe.edu.utp.hlev.bancosangre.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RBAC (RF-02): nivel de aprobación/supervisión
 * (Jefe de Banco de Sangre, Administrador). Para acciones de mayor impacto,
 * como aprobar intercambios interhospitalarios.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("hasAnyAuthority('Jefe de Banco de Sangre', 'Administrador')")
public @interface SoloSupervisionBancoSangre {
}
