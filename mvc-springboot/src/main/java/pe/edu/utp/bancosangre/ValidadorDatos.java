package pe.edu.utp.bancosangre;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Validación del lado del servidor. El navegador ya impide escribir datos
 * inválidos (validacion.js), pero esa protección se puede saltar, así que aquí
 * se vuelven a revisar los datos antes de guardarlos en PostgreSQL.
 * Devuelve el primer error encontrado o null si todo está correcto.
 */
public final class ValidadorDatos {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern TELEFONO = Pattern.compile("^9\\d{8}$");
    private static final Pattern CORREO_PERSONAL = Pattern.compile("^[A-Za-z0-9._+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");
    private static final Pattern DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} .,'’-]{1,118}$");
    private static final Pattern CORREO = Pattern.compile("^[a-z0-9._-]+@hlev\\.gob\\.pe$");
    private static final Pattern HC = Pattern.compile("^HC-\\d{4}-\\d{5}$");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private ValidadorDatos() { }

    public static String validar(String cuerpo) {
        JsonNode raiz;
        try {
            raiz = JSON.readTree(cuerpo);
        } catch (Exception e) {
            return "Los datos enviados no tienen un formato válido";
        }
        if (raiz == null || !raiz.isObject()) return "Los datos enviados no tienen un formato válido";

        for (JsonNode u : lista(raiz, "usuarios")) {
            String n = texto(u, "nombre");
            if (!NOMBRE.matcher(n).matches()) return "Usuario: el nombre solo puede contener letras y espacios";
            if (!DNI.matcher(texto(u, "dni")).matches()) return "Usuario " + n + ": el DNI debe tener exactamente 8 dígitos";
            if (!CORREO.matcher(texto(u, "correo")).matches()) return "Usuario " + n + ": el correo debe ser institucional (@hlev.gob.pe)";
            if (texto(u, "rol").isEmpty()) return "Usuario " + n + ": el rol es obligatorio";
        }
        for (JsonNode d : lista(raiz, "donantes")) {
            String n = texto(d, "nombre");
            if (!NOMBRE.matcher(n).matches()) return "Donante: el nombre solo puede contener letras y espacios";
            if (!DNI.matcher(texto(d, "dni")).matches()) return "Donante " + n + ": el DNI debe tener exactamente 8 dígitos";
            if (!"M".equals(texto(d, "sexo")) && !"F".equals(texto(d, "sexo")))
                return "Donante " + n + ": el sexo es obligatorio (M o F)";
            if (!TELEFONO.matcher(texto(d, "telefono")).matches())
                return "Donante " + n + ": el teléfono debe tener 9 dígitos y empezar con 9";
            if (!CORREO_PERSONAL.matcher(texto(d, "correo")).matches())
                return "Donante " + n + ": el correo electrónico no es válido";
            if (texto(d, "direccion").length() < 5)
                return "Donante " + n + ": la dirección es obligatoria";
            String fn = texto(d, "fechaNacimiento");
            if (fn.isEmpty() || fn.equals("—")) return "Donante " + n + ": la fecha de nacimiento es obligatoria";
            try { LocalDate.parse(fn, FECHA); }
            catch (DateTimeException e) { return "Donante " + n + ": la fecha de nacimiento debe ser real y tener el formato dd/mm/aaaa"; }
            JsonNode peso = d.get("pesoKg");
            if (peso == null || !peso.isNumber()) return "Donante " + n + ": el peso es obligatorio";
            if (peso.asDouble() < 30 || peso.asDouble() > 250)
                return "Donante " + n + ": el peso debe estar entre 30 y 250 kg";
            JsonNode hb = d.get("hemoglobina");
            if (hb == null || !hb.isNumber()) return "Donante " + n + ": la hemoglobina es obligatoria";
            // Si está diferido, la base exige el motivo y el tipo de diferimiento
            if (texto(d, "antecedentes").isEmpty())
                return "Donante " + n + ": los antecedentes son obligatorios (escribe Ninguno si no hay)";
            if (texto(d, "estado").contains("Diferido") || texto(d, "estado").contains("Excluido")) {
                if (texto(d, "motivoDiferimiento").isEmpty() || texto(d, "tipoDiferimiento").isEmpty())
                    return "Donante " + n + ": un donante diferido debe tener motivo y tipo de diferimiento";
                String vig = texto(d, "vigenciaDiferimiento");
                if ("Temporal".equals(texto(d, "tipoDiferimiento")) && (vig.isEmpty() || vig.equals("—")))
                    return "Donante " + n + ": un diferimiento temporal debe indicar la fecha en que termina";
            }
        }
        for (JsonNode pa : lista(raiz, "pacientes")) {
            if (!NOMBRE.matcher(texto(pa, "paciente")).matches())
                return "Paciente: el nombre solo puede contener letras y espacios";
            if (!DNI.matcher(texto(pa, "dni")).matches()) return "Paciente: el DNI debe tener exactamente 8 dígitos";
            if (!HC.matcher(texto(pa, "hc")).matches()) return "Paciente: la historia clínica debe tener el formato HC-AAAA-00000";
            String np = texto(pa, "paciente");
            if (!"M".equals(texto(pa, "sexo")) && !"F".equals(texto(pa, "sexo")))
                return "Paciente " + np + ": el sexo es obligatorio (M o F)";
            if (!TELEFONO.matcher(texto(pa, "telefono")).matches())
                return "Paciente " + np + ": el teléfono debe tener 9 dígitos y empezar con 9";
            if (!CORREO_PERSONAL.matcher(texto(pa, "correo")).matches())
                return "Paciente " + np + ": el correo electrónico no es válido";
            if (texto(pa, "direccion").length() < 5) return "Paciente " + np + ": la dirección es obligatoria";
            try { LocalDate.parse(texto(pa, "fechaNacimiento"), FECHA); }
            catch (DateTimeException e) { return "Paciente " + np + ": la fecha de nacimiento debe ser real y tener el formato dd/mm/aaaa"; }
            JsonNode pesoP = pa.get("pesoKg");
            if (pesoP == null || !pesoP.isNumber() || pesoP.asDouble() < 2 || pesoP.asDouble() > 250)
                return "Paciente " + np + ": el peso es obligatorio (entre 2 y 250 kg)";
            JsonNode hbP = pa.get("hemoglobina");
            if (hbP == null || !hbP.isNumber()) return "Paciente " + np + ": la hemoglobina es obligatoria";
        }
        for (JsonNode s : lista(raiz, "solicitudes")) {
            String p = texto(s, "paciente");
            if (!NOMBRE.matcher(p).matches()) return "Solicitud: el nombre del paciente solo puede contener letras y espacios";
            if (!DNI.matcher(texto(s, "dni")).matches())
                return "Solicitud de " + p + ": el DNI del paciente es obligatorio y debe tener exactamente 8 dígitos";
            if (!HC.matcher(texto(s, "hc")).matches()) return "Solicitud de " + p + ": la historia clínica debe tener el formato HC-AAAA-00000";
            if (texto(s, "cie10").isEmpty() || texto(s, "cie10").equals("—"))
                return "Solicitud de " + p + ": el diagnóstico CIE-10 es obligatorio";
            if (texto(s, "componente").isEmpty())
                return "Solicitud de " + p + ": el hemocomponente y la cantidad son obligatorios";
            // Campo condicional: la fecha solo se exige en una reserva o en una solicitud programada
            boolean pideFecha = "reserva".equals(texto(s, "tipo")) || "Programada".equals(texto(s, "prioridad"));
            if (pideFecha) {
                String f = texto(s, "reservaFecha");
                if (f.isEmpty()) return "Solicitud de " + p + ": una reserva o solicitud programada debe indicar su fecha";
                try { LocalDate.parse(f, FECHA); }
                catch (DateTimeException e) { return "Solicitud de " + p + ": la fecha debe ser real y tener el formato dd/mm/aaaa"; }
            }
        }
        for (JsonNode h : lista(raiz, "hemovigilancia")) {
            if (!NOMBRE.matcher(texto(h, "paciente")).matches())
                return "Hemovigilancia: el nombre del paciente solo puede contener letras y espacios";
            if (texto(h, "reaccion").isEmpty()) return "Hemovigilancia: la reacción observada es obligatoria";
            if (texto(h, "din").isEmpty() || texto(h, "din").equals("—"))
                return "Hemovigilancia: el evento debe asociarse a un DIN de origen";
        }
        for (JsonNode i : lista(raiz, "intercambios")) {
            JsonNode u = i.get("unidades");
            if (u != null && u.isNumber() && (u.asInt() < 1 || u.asInt() > 50))
                return "Intercambio: la cantidad debe ser un entero entre 1 y 50";
        }
        return null;
    }

    private static Iterable<JsonNode> lista(JsonNode raiz, String clave) {
        JsonNode n = raiz.get(clave);
        return (n != null && n.isArray()) ? n : java.util.List.<JsonNode>of();
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull()) ? "" : v.asText("").trim();
    }
}
