-- RF-30: historial completo del ciclo de vida de una unidad.
CREATE TABLE hemocomponente_eventos (
    id BIGSERIAL PRIMARY KEY,
    hemocomponente_id BIGINT NOT NULL REFERENCES hemocomponentes(id),
    tipo_evento VARCHAR(30) NOT NULL,
    detalle VARCHAR(500),
    usuario_id BIGINT REFERENCES usuarios(id),
    fecha TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_hemocomponente_eventos_unidad ON hemocomponente_eventos(hemocomponente_id, fecha);

-- RF-29: motivo y evidencia fotográfica del descarte de una unidad.
CREATE TABLE descartes_hemocomponente (
    id BIGSERIAL PRIMARY KEY,
    hemocomponente_id BIGINT NOT NULL UNIQUE REFERENCES hemocomponentes(id),
    motivo VARCHAR(30) NOT NULL,
    evidencia_foto TEXT,
    usuario_id BIGINT REFERENCES usuarios(id),
    fecha TIMESTAMP NOT NULL DEFAULT now()
);
