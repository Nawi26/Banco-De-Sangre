-- RF-03/RF-04: ficha clínica integral y aptitud/diferimiento automático del donante.
ALTER TABLE donantes
    ADD COLUMN telefono VARCHAR(20),
    ADD COLUMN direccion VARCHAR(255),
    ADD COLUMN peso_kg NUMERIC(5,2),
    ADD COLUMN talla_cm NUMERIC(5,2),
    ADD COLUMN presion_sistolica INTEGER,
    ADD COLUMN presion_diastolica INTEGER,
    ADD COLUMN pulso INTEGER,
    ADD COLUMN hemoglobina NUMERIC(4,2),
    ADD COLUMN apto BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN tipo_diferimiento VARCHAR(20),
    ADD COLUMN motivo_diferimiento VARCHAR(255),
    ADD COLUMN diferido_hasta DATE,
    ADD COLUMN fecha_ultima_donacion DATE;

-- RF-23/RF-35: campañas externas de donación.
CREATE TABLE campanas_donacion (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    institucion VARCHAR(200),
    tipo VARCHAR(50),
    fecha_inicio DATE,
    fecha_fin DATE,
    meta_unidades INTEGER,
    unidades_logradas INTEGER NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE donaciones ADD COLUMN campana_id BIGINT REFERENCES campanas_donacion(id);

-- RF-03/RF-04: histórico de cuestionarios de triaje y su resultado.
CREATE TABLE cuestionarios_triaje (
    id BIGSERIAL PRIMARY KEY,
    donante_id BIGINT NOT NULL REFERENCES donantes(id),
    donacion_id BIGINT REFERENCES donaciones(id),
    antecedente_its BOOLEAN NOT NULL DEFAULT FALSE,
    antecedente_uso_drogas BOOLEAN NOT NULL DEFAULT FALSE,
    tatuaje_o_perforacion_reciente BOOLEAN NOT NULL DEFAULT FALSE,
    embarazo_o_parto_reciente BOOLEAN NOT NULL DEFAULT FALSE,
    viaje_zona_endemica BOOLEAN NOT NULL DEFAULT FALSE,
    otros_antecedentes VARCHAR(500),
    resultado_apto BOOLEAN NOT NULL,
    resultado_tipo_diferimiento VARCHAR(20),
    resultado_motivo VARCHAR(255),
    resultado_diferido_hasta DATE,
    evaluador_id BIGINT REFERENCES usuarios(id),
    fecha_evaluacion TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_triaje_donante ON cuestionarios_triaje(donante_id);

-- RF-38: consentimiento informado digital previo a la extracción.
CREATE TABLE consentimientos_informados (
    id BIGSERIAL PRIMARY KEY,
    donante_id BIGINT NOT NULL REFERENCES donantes(id),
    donacion_id BIGINT REFERENCES donaciones(id),
    tipo_validacion VARCHAR(20) NOT NULL,
    evidencia_validacion TEXT NOT NULL,
    aceptado BOOLEAN NOT NULL,
    ip_origen VARCHAR(64),
    usuario_registro_id BIGINT REFERENCES usuarios(id),
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_consentimiento_donante ON consentimientos_informados(donante_id);

-- RF-22: citas de donación (programación, reprogramación y recordatorio).
CREATE TABLE citas_donacion (
    id BIGSERIAL PRIMARY KEY,
    donante_id BIGINT NOT NULL REFERENCES donantes(id),
    campana_id BIGINT REFERENCES campanas_donacion(id),
    fecha_hora TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    notas VARCHAR(500),
    creado_en TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMP
);
CREATE INDEX idx_citas_donante ON citas_donacion(donante_id);
CREATE INDEX idx_citas_fecha_hora ON citas_donacion(fecha_hora);
