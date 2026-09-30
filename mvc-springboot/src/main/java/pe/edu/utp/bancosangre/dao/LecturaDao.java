package pe.edu.utp.bancosangre.dao;

import java.util.List;

/**
 * Patrón DAO (Data Access Object) — contrato de LECTURA.
 *
 * Cada entidad del sistema (donantes, usuarios, unidades de sangre, ...) tiene su propio DAO.
 * El DAO es la ÚNICA clase que conoce cómo se guarda y se lee esa entidad en PostgreSQL; el
 * resto de la aplicación (servicio y controladores) solo usa estas interfaces.
 *
 * Un registro es un Map (nombre del campo -> valor), porque la lógica de conversión entre las
 * tablas normalizadas y lo que muestra la pantalla vive en las funciones de la base (db/funciones.sql).
 */
public interface LecturaDao {

    /** Nombre de la colección que atiende este DAO (usuarios, donantes, inventario, ...). */
    String coleccion();

    /** Todos los registros de la entidad, listos para la pantalla. */
    List<Object> listar();
}
