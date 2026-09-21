-- RF-42: turnos y disponibilidad del personal técnico.
CREATE TABLE turnos_personal (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    tipo_turno VARCHAR(20) NOT NULL,
    observaciones TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_turnos_usuario ON turnos_personal(usuario_id);

-- RF-47: mantenimiento preventivo y calibración de equipos críticos.
CREATE TABLE mantenimientos_equipo (
    id BIGSERIAL PRIMARY KEY,
    nombre_equipo VARCHAR(100) NOT NULL,
    tipo_equipo VARCHAR(30) NOT NULL,
    tipo_mantenimiento VARCHAR(20) NOT NULL,
    fecha_realizado TIMESTAMP NOT NULL,
    fecha_proximo_vencimiento TIMESTAMP,
    responsable_id BIGINT REFERENCES usuarios(id),
    observaciones TEXT
);

-- RF-49: control de versiones de protocolos y guías clínicas vigentes.
CREATE TABLE protocolos_clinicos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    version VARCHAR(20) NOT NULL,
    contenido_url TEXT,
    notas_cambio TEXT,
    vigente BOOLEAN NOT NULL DEFAULT true,
    publicado_por_id BIGINT REFERENCES usuarios(id),
    fecha_publicacion TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_protocolos_nombre ON protocolos_clinicos(nombre);

-- RF-34: respaldos automáticos de la base de datos, con verificación de integridad.
CREATE TABLE respaldos_base_datos (
    id BIGSERIAL PRIMARY KEY,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP,
    ruta_archivo TEXT,
    tamanio_bytes BIGINT,
    hash_integridad VARCHAR(64),
    estado VARCHAR(20) NOT NULL,
    detalle TEXT
);

-- RF-39: monitoreo de disponibilidad (uptime) de la plataforma.
CREATE TABLE eventos_disponibilidad (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT now()
);
