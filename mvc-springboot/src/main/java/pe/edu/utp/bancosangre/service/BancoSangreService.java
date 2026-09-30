package pe.edu.utp.bancosangre.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.bancosangre.dao.LecturaDao;
import pe.edu.utp.bancosangre.dao.SincronizacionDao;
import pe.edu.utp.bancosangre.dao.UsuarioDao;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Capa de servicio: los controladores hablan con este servicio y el servicio con los DAO.
 * Flujo del sistema:   Controlador -> Servicio -> DAO -> PostgreSQL
 */
@Service
public class BancoSangreService {

    private final Map<String, LecturaDao> lecturas;
    private final SincronizacionDao sincronizacion;
    private final UsuarioDao usuarios;

    /** Spring entrega aquí TODOS los DAO de lectura (donantes, usuarios, inventario...). */
    public BancoSangreService(List<LecturaDao> daosDeLectura, SincronizacionDao sincronizacion, UsuarioDao usuarios) {
        this.lecturas = daosDeLectura.stream().collect(Collectors.toMap(LecturaDao::coleccion, Function.identity()));
        this.sincronizacion = sincronizacion;
        this.usuarios = usuarios;
    }

    /** Registros de una colección (usuarios, donantes, inventario, solicitudes, ...). */
    public List<Object> listar(String coleccion) {
        LecturaDao dao = lecturas.get(coleccion);
        if (dao == null) {
            throw new IllegalArgumentException("Colección desconocida: " + coleccion);
        }
        return dao.listar();
    }

    /** Guarda en una sola transacción todo lo que cambió en la pantalla. */
    public void sincronizar(String cambiosJson, String correoActor, String rolActor) {
        sincronizacion.sincronizar(cambiosJson, correoActor, rolActor);
    }

    /** Inicio de sesión: devuelve {nombre, correo, rol} o null si usuario o contraseña no son correctos. */
    public Map<String, Object> autenticar(String usuario, String clave) {
        return usuarios.autenticar(usuario, clave);
    }

    public void registrarAcceso(String usuario) {
        usuarios.registrarAcceso(usuario);
    }

    /** Restablece los datos de ejemplo (solo Administrador). */
    public void vaciar() {
        sincronizacion.vaciar();
    }
}
