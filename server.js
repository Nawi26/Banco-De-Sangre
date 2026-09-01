// ============================================================================
// SERVIDOR NODE.JS - BANCO DE SANGRE HLEV (Backend)
// ============================================================================

const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');

// 1. Inicialización
const app = express();
app.use(cors());
app.use(express.json());

// 2. Configuración de PostgreSQL (Reemplaza 'tu_password_seguro' por tu clave real)
const pool = new Pool({
    user: 'postgres',
    host: 'localhost',
    database: 'BancoSangreDb',
    password: '12345678',
    port: 5432,
});

pool.connect()
    .then(() => console.log('Conexión exitosa a la base de datos PostgreSQL'))
    .catch(err => console.error('Error de conexión a la BD', err.stack));

// ============================================================================
// SERVICIOS WEB RESTful (Endpoints)
// ============================================================================

// A. MÓDULO DE AUTENTICACIÓN (Login con validaciones estrictas)
app.post('/api/login', async (req, res) => {
    const { identificador, password } = req.body;

    try {
        const query = `
            SELECT u.id, u.nombres, u.apellidos, u.password, u.estado, r.nombre AS rol_nombre 
            FROM usuarios u
            INNER JOIN roles r ON u.rol_id = r.id
            WHERE (u.dni = $1 OR u.email = $1)
        `;
        const { rows } = await pool.query(query, [identificador]);

        // Validación 1: ¿Existe la cuenta en la base de datos?
        if (rows.length === 0) {
            return res.status(404).json({ 
                error: true, 
                mensaje: "Error: No existe una cuenta vinculada a este usuario." 
            });
        }

        const usuario = rows[0];

        // Validación 2: ¿La cuenta está activa?
        if (!usuario.estado) {
            return res.status(403).json({ 
                error: true, 
                mensaje: "Acceso denegado: Esta cuenta ha sido deshabilitada." 
            });
        }

        // Validación 3: ¿La contraseña es correcta?
        if (usuario.password !== password) {
            return res.status(401).json({ 
                error: true, 
                mensaje: "Credenciales incorrectas. Verifique su contraseña." 
            });
        }

        // Acceso Concedido
        res.status(200).json({
            exito: true,
            usuario: {
                id: usuario.id,
                nombre_completo: `${usuario.nombres} ${usuario.apellidos}`,
                rol: usuario.rol_nombre
            }
        });

    } catch (error) {
        console.error("Error en /api/login:", error);
        res.status(500).json({ error: true, mensaje: "Error interno del servidor." });
    }
});

// B. MÓDULO DE INVENTARIO (Algoritmo FEFO)
app.get('/api/inventario', async (req, res) => {
    try {
        const query = `
            SELECT 
                codigo_producto_isbt, 
                tipo_hemocomponente, 
                grupo_abo, 
                factor_rh, 
                fecha_vencimiento, 
                estado 
            FROM hemocomponentes 
            WHERE estado = 'Aprobado' 
            ORDER BY fecha_vencimiento ASC;
        `;
        const { rows } = await pool.query(query);
        res.status(200).json(rows);
    } catch (error) {
        console.error("Error en /api/inventario:", error);
        res.status(500).json({ error: true, mensaje: "Error al consultar inventario" });
    }
});

// C. MÓDULO RED INTERHOSPITALARIA
app.get('/api/red-interhospitalaria/:codigo', async (req, res) => {
    try {
        const query = `
            SELECT codigo_producto_isbt, grupo_abo, factor_rh, estado, ubicacion_fisica
            FROM hemocomponentes
            WHERE codigo_producto_isbt = $1;
        `;
        const { rows } = await pool.query(query, [req.params.codigo]);
        
        if (rows.length > 0) res.status(200).json(rows[0]);
        else res.status(404).json({ error: true, mensaje: "Unidad no encontrada en la red." });
    } catch (error) {
        console.error("Error en búsqueda interhospitalaria:", error);
        res.status(500).json({ error: true, mensaje: "Error en el motor de búsqueda" });
    }
});

// 3. Encender Servidor
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Servidor backend operando en http://localhost:${PORT}`);
});