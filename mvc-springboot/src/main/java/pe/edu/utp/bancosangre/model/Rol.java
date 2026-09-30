package pe.edu.utp.bancosangre.model;

import java.util.List;
import java.util.Map;

/**
 * Roles del sistema y las vistas a las que cada uno tiene acceso (control de
 * acceso basado en roles - RBAC), aplicado en el servidor (Controlador) en
 * lugar de en JavaScript.
 */
public enum Rol {
    MEDICO_SOLICITANTE("Médico Solicitante"),
    TECNOLOGO_MEDICO("Tecnólogo Médico"),
    JEFE_BANCO_SANGRE("Jefe de Banco de Sangre"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    private static final Map<Rol, List<String>> VISTAS_PERMITIDAS = Map.of(
            MEDICO_SOLICITANTE, List.of("dashboard", "pacientes", "solicitudes", "movil"),
            TECNOLOGO_MEDICO, List.of("dashboard", "inventario", "donantes", "pacientes", "etiquetado", "solicitudes", "hemovigilancia"),
            // El Jefe de Banco de Sangre opera el area tecnica y logistica, pero NO ve Auditoria.
            JEFE_BANCO_SANGRE, List.of("dashboard", "inventario", "donantes", "pacientes", "etiquetado", "solicitudes",
                    "hemovigilancia", "despacho", "interhospitalario", "movil", "reportes"),
            // El Administrador ve todas las pantallas del sistema.
            ADMINISTRADOR, List.of("dashboard", "inventario", "donantes", "pacientes", "etiquetado", "solicitudes",
                    "despacho", "interhospitalario", "movil", "hemovigilancia", "reportes", "auditoria", "administracion")
    );

    public List<String> vistasPermitidas() {
        return VISTAS_PERMITIDAS.get(this);
    }

    public boolean puedeVer(String vista) {
        return vistasPermitidas().contains(vista);
    }

    public static Rol desdeEtiqueta(String etiqueta) {
        for (Rol r : values()) {
            if (r.etiqueta.equals(etiqueta)) return r;
        }
        throw new IllegalArgumentException("Rol desconocido: " + etiqueta);
    }
}
