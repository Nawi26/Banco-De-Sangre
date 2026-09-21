package pe.edu.utp.hlev.bancosangre.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hlev.bancosangre.dto.RespaldoBaseDatosDTO;
import pe.edu.utp.hlev.bancosangre.dto.VerificacionIntegridadDTO;
import pe.edu.utp.hlev.bancosangre.model.Donacion;
import pe.edu.utp.hlev.bancosangre.model.Donante;
import pe.edu.utp.hlev.bancosangre.model.RespaldoBaseDatos;
import pe.edu.utp.hlev.bancosangre.model.Solicitud;
import pe.edu.utp.hlev.bancosangre.model.Usuario;
import pe.edu.utp.hlev.bancosangre.repository.DonacionRepository;
import pe.edu.utp.hlev.bancosangre.repository.DonanteRepository;
import pe.edu.utp.hlev.bancosangre.repository.RespaldoBaseDatosRepository;
import pe.edu.utp.hlev.bancosangre.repository.SolicitudRepository;
import pe.edu.utp.hlev.bancosangre.repository.UsuarioRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

/**
 * RF-34: respaldo lógico automático y programado de las tablas clínicas críticas,
 * con verificación de integridad mediante hash SHA-256. El respaldo se escribe en un
 * archivo de texto local (no un dump binario del motor de BD), lo que mantiene la
 * implementación portable entre entornos sin depender de que `pg_dump` esté instalado.
 */
@Service
public class RespaldoService {

    private final RespaldoBaseDatosRepository respaldoBaseDatosRepository;
    private final UsuarioRepository usuarioRepository;
    private final DonanteRepository donanteRepository;
    private final DonacionRepository donacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final String directorioBackups;

    public RespaldoService(RespaldoBaseDatosRepository respaldoBaseDatosRepository, UsuarioRepository usuarioRepository,
                            DonanteRepository donanteRepository, DonacionRepository donacionRepository,
                            SolicitudRepository solicitudRepository,
                            @Value("${app.backups.directorio}") String directorioBackups) {
        this.respaldoBaseDatosRepository = respaldoBaseDatosRepository;
        this.usuarioRepository = usuarioRepository;
        this.donanteRepository = donanteRepository;
        this.donacionRepository = donacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.directorioBackups = directorioBackups;
    }

    public List<RespaldoBaseDatosDTO> listarHistorial() {
        return respaldoBaseDatosRepository.findAllByOrderByFechaInicioDesc().stream().map(RespaldoBaseDatosDTO::from).toList();
    }

    /** RF-34: respaldo automático diario, de madrugada. */
    @Scheduled(cron = "0 0 1 * * *")
    public void ejecutarRespaldoAutomatico() {
        ejecutarRespaldo();
    }

    @Transactional
    public RespaldoBaseDatosDTO ejecutarRespaldo() {
        RespaldoBaseDatos respaldo = new RespaldoBaseDatos();
        respaldo.setFechaInicio(LocalDateTime.now());
        respaldo.setEstado("EN_PROGRESO");
        respaldoBaseDatosRepository.save(respaldo);

        try {
            Path carpeta = Path.of(directorioBackups);
            Files.createDirectories(carpeta);

            String nombreArchivo = "respaldo-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now()) + ".csv";
            Path archivo = carpeta.resolve(nombreArchivo);

            String contenido = generarContenidoRespaldo();
            Files.writeString(archivo, contenido, StandardCharsets.UTF_8);

            long tamanio = Files.size(archivo);
            String hash = calcularSha256(archivo);

            respaldo.setFechaFin(LocalDateTime.now());
            respaldo.setRutaArchivo(archivo.toAbsolutePath().toString());
            respaldo.setTamanioBytes(tamanio);
            respaldo.setHashIntegridad(hash);
            respaldo.setEstado("EXITOSO");
            respaldo.setDetalle("Respaldo lógico de usuarios, donantes, donaciones y solicitudes.");
        } catch (IOException | NoSuchAlgorithmException ex) {
            respaldo.setFechaFin(LocalDateTime.now());
            respaldo.setEstado("FALLIDO");
            respaldo.setDetalle("Error al generar el respaldo: " + ex.getMessage());
        }

        respaldoBaseDatosRepository.save(respaldo);
        return RespaldoBaseDatosDTO.from(respaldo);
    }

    /** RF-34: verificación periódica de integridad — recalcula el hash del archivo y lo compara con el registrado. */
    public VerificacionIntegridadDTO verificarIntegridad(Long respaldoId) {
        RespaldoBaseDatos respaldo = respaldoBaseDatosRepository.findById(respaldoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Respaldo no encontrado."));

        if (respaldo.getRutaArchivo() == null || respaldo.getHashIntegridad() == null) {
            return new VerificacionIntegridadDTO(respaldoId, false, "Este respaldo no tiene archivo o hash registrado.");
        }

        Path archivo = Path.of(respaldo.getRutaArchivo());
        if (!Files.exists(archivo)) {
            return new VerificacionIntegridadDTO(respaldoId, false, "El archivo de respaldo ya no existe en la ruta registrada.");
        }

        try {
            String hashActual = calcularSha256(archivo);
            boolean integro = hashActual.equals(respaldo.getHashIntegridad());
            return new VerificacionIntegridadDTO(respaldoId, integro,
                    integro ? "El archivo conserva la integridad registrada al momento del respaldo."
                            : "¡Alerta! El hash actual no coincide con el registrado: el archivo pudo haberse alterado.");
        } catch (IOException | NoSuchAlgorithmException ex) {
            return new VerificacionIntegridadDTO(respaldoId, false, "No se pudo recalcular el hash: " + ex.getMessage());
        }
    }

    private String generarContenidoRespaldo() {
        StringBuilder csv = new StringBuilder();
        csv.append("RESPALDO_LOGICO_HLEV;").append(LocalDateTime.now()).append("\n\n");

        csv.append("USUARIOS (sin contraseña)\nid;dni;nombres;apellidos;email;rol\n");
        for (Usuario u : usuarioRepository.findAll()) {
            csv.append(u.getId()).append(";").append(u.getDni()).append(";").append(u.getNombres()).append(";")
                    .append(u.getApellidos()).append(";").append(u.getEmail()).append(";")
                    .append(u.getRol() != null ? u.getRol().getNombre() : "").append("\n");
        }

        csv.append("\nDONANTES\nid;num_doc;nombres;apellidos;grupo_abo;factor_rh;apto\n");
        for (Donante d : donanteRepository.findAll()) {
            csv.append(d.getId()).append(";").append(d.getNumDoc()).append(";").append(d.getNombres()).append(";")
                    .append(d.getApellidos()).append(";").append(d.getGrupoAbo()).append(";").append(d.getFactorRh()).append(";")
                    .append(d.getApto()).append("\n");
        }

        csv.append("\nDONACIONES\nid;din_isbt128;donante_id;fecha_extraccion;tamizaje_aprobado\n");
        for (Donacion don : donacionRepository.findAll()) {
            csv.append(don.getId()).append(";").append(don.getDinIsbt128()).append(";")
                    .append(don.getDonante() != null ? don.getDonante().getId() : "").append(";")
                    .append(don.getFechaExtraccion()).append(";").append(don.getTamizajeAprobado()).append("\n");
        }

        csv.append("\nSOLICITUDES\nid;codigo_solicitud;tipo_hemocomponente;prioridad;estado;fecha_solicitud\n");
        for (Solicitud s : solicitudRepository.findAll()) {
            csv.append(s.getId()).append(";").append(s.getCodigoSolicitud()).append(";").append(s.getTipoHemocomponente()).append(";")
                    .append(s.getPrioridad()).append(";").append(s.getEstado()).append(";").append(s.getFechaSolicitud()).append("\n");
        }

        return csv.toString();
    }

    private String calcularSha256(Path archivo) throws IOException, NoSuchAlgorithmException {
        byte[] contenido = Files.readAllBytes(archivo);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(contenido);
        return HexFormat.of().formatHex(hash);
    }
}
