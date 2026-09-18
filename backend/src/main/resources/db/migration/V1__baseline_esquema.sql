-- Baseline: reconstruye el esquema heredado de la versión Node/JS.
-- En la base de datos actual del HLEV este esquema ya existe y Flyway lo omite
-- (spring.flyway.baseline-on-migrate=true + baseline-version=1).
-- Este archivo sólo se ejecuta en instalaciones nuevas (entornos de desarrollo o pruebas),
-- de modo que el proyecto pueda desplegarse desde cero sin depender de un dump manual.

CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) UNIQUE,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    colegiatura VARCHAR(50),
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    rol_id BIGINT NOT NULL REFERENCES roles(id)
);

CREATE TABLE ipress_establecimientos (
    id BIGINT PRIMARY KEY,
    codigo_renipress VARCHAR(50) UNIQUE,
    nombre_establecimiento VARCHAR(200) NOT NULL,
    categoria_ipress VARCHAR(50),
    tipo_banco_sangre VARCHAR(50)
);

CREATE TABLE donantes (
    id BIGSERIAL PRIMARY KEY,
    tipo_doc VARCHAR(20) NOT NULL,
    num_doc VARCHAR(20) NOT NULL,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE,
    sexo VARCHAR(10),
    grupo_abo VARCHAR(5),
    factor_rh VARCHAR(10),
    estado_diferido BOOLEAN NOT NULL DEFAULT FALSE,
    es_ocupacion_riesgo BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tipo_doc, num_doc)
);

CREATE TABLE donaciones (
    id BIGSERIAL PRIMARY KEY,
    din_isbt128 VARCHAR(50) UNIQUE,
    donante_id BIGINT NOT NULL REFERENCES donantes(id),
    fecha_extraccion TIMESTAMP,
    volumen_ml INTEGER,
    tipo_donacion VARCHAR(30),
    tamizaje_aprobado BOOLEAN
);

CREATE TABLE hemocomponentes (
    id BIGSERIAL PRIMARY KEY,
    codigo_producto_isbt VARCHAR(50) UNIQUE,
    tipo_hemocomponente VARCHAR(50),
    grupo_abo VARCHAR(5),
    factor_rh VARCHAR(10),
    volumen_ml INTEGER,
    fecha_vencimiento TIMESTAMP,
    ubicacion_fisica VARCHAR(100),
    estado VARCHAR(30)
);

CREATE TABLE pacientes (
    id BIGSERIAL PRIMARY KEY,
    tipo_doc VARCHAR(20) NOT NULL,
    num_doc VARCHAR(20) NOT NULL,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE,
    sexo VARCHAR(10),
    grupo_abo VARCHAR(5),
    factor_rh VARCHAR(10),
    UNIQUE (tipo_doc, num_doc)
);

CREATE TABLE solicitudes (
    id BIGSERIAL PRIMARY KEY,
    codigo_solicitud VARCHAR(50) UNIQUE,
    paciente_id BIGINT NOT NULL REFERENCES pacientes(id),
    medico_id BIGINT NOT NULL REFERENCES usuarios(id),
    tipo_hemocomponente VARCHAR(50),
    unidades_solicitadas INTEGER,
    prioridad VARCHAR(20),
    estado VARCHAR(20),
    indicacion_clinica VARCHAR(500),
    fecha_solicitud TIMESTAMP
);

CREATE TABLE transfusiones (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes(id),
    hemocomponente_id BIGINT NOT NULL REFERENCES hemocomponentes(id),
    paciente_id BIGINT NOT NULL REFERENCES pacientes(id),
    tecnologo_id BIGINT REFERENCES usuarios(id),
    medico_id BIGINT REFERENCES usuarios(id),
    resultado_prueba_cruzada VARCHAR(30),
    fecha_transfusion TIMESTAMP,
    reaccion_adversa BOOLEAN NOT NULL DEFAULT FALSE,
    detalles_reaccion VARCHAR(500)
);

CREATE TABLE solicitudes_intercambio (
    id BIGSERIAL PRIMARY KEY,
    ipress_solicitante_id BIGINT NOT NULL REFERENCES ipress_establecimientos(id),
    ipress_proveedora_id BIGINT NOT NULL REFERENCES ipress_establecimientos(id),
    hemocomponente_id BIGINT NOT NULL REFERENCES hemocomponentes(id),
    estado VARCHAR(20),
    temperatura_cadena_frio DOUBLE PRECISION,
    responsable_transporte VARCHAR(150),
    fecha_solicitud TIMESTAMP,
    fecha_respuesta TIMESTAMP
);
