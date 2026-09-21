-- RF-45: convenios y contratos interinstitucionales vigentes para el intercambio de hemocomponentes.
CREATE TABLE convenios_interinstitucionales (
    id BIGSERIAL PRIMARY KEY,
    ipress_id BIGINT NOT NULL REFERENCES ipress_establecimientos(id),
    numero_convenio VARCHAR(60) NOT NULL,
    objeto TEXT NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);
