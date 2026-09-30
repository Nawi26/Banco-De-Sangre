package pe.edu.utp.bancosangre.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalogo de pantallas del sistema: clave de ruta -> (titulo, subtitulo, plantilla).
 * Reemplaza al objeto viewTitles de la versión original; ahora vive en el Controlador/Modelo
 * en lugar de en JavaScript, y alimenta tanto el menu lateral como el topbar de cada Vista.
 */
public final class Vista {

    public record Info(String clave, String titulo, String subtitulo) {}

    public static final Map<String, Info> CATALOGO = new LinkedHashMap<>();

    static {
        put("dashboard", "Panel general", "Hospital de Lima Este Vitarte · Banco de Sangre");
        put("inventario", "Inventario", "Unidades guardadas en las cámaras de refrigeración y congeladoras");
        put("donantes", "Donantes", "Registro del donante, diferimientos y resultados de laboratorio");
        put("pacientes", "Pacientes", "Receptores de sangre, su DNI, historia clínica y solicitudes");
        put("etiquetado", "Etiquetado", "Generación de la etiqueta de cada unidad");
        put("solicitudes", "Solicitudes", "Pedidos de sangre de los servicios del hospital");
        put("despacho", "Despacho", "Verificación y entrega de la unidad al servicio");
        put("interhospitalario", "Red de hospitales", "Stock e intercambio de unidades con otros establecimientos");
        put("movil", "Aplicación móvil", "El mismo sistema, en versión para celular");
        put("hemovigilancia", "Hemovigilancia", "Reacciones presentadas después de una transfusión");
        put("reportes", "Reportes", "Consolidados del servicio para descargar o enviar");
        put("auditoria", "Auditoría", "Qué hizo cada usuario dentro del sistema");
        put("administracion", "Administración", "Usuarios del sistema y el rol de cada uno");
    }

    private static void put(String clave, String titulo, String subtitulo) {
        CATALOGO.put(clave, new Info(clave, titulo, subtitulo));
    }

    private Vista() {}
}
