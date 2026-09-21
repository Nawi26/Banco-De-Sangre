-- RF-15: eventos adversos transfusionales (inmediatos o tardíos).
CREATE TABLE eventos_adversos_transfusion (
    id BIGSERIAL PRIMARY KEY,
    transfusion_id BIGINT NOT NULL REFERENCES transfusiones(id),
    tipo_reaccion VARCHAR(30) NOT NULL,
    es_inmediata BOOLEAN NOT NULL,
    gravedad VARCHAR(20) NOT NULL,
    descripcion TEXT NOT NULL,
    acciones_tomadas TEXT,
    usuario_id BIGINT REFERENCES usuarios(id),
    fecha_deteccion TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_eventos_transfusion ON eventos_adversos_transfusion(transfusion_id);

-- RF-43: eventos adversos durante o después de la donación.
CREATE TABLE eventos_adversos_donacion (
    id BIGSERIAL PRIMARY KEY,
    donacion_id BIGINT NOT NULL REFERENCES donaciones(id),
    tipo_evento VARCHAR(30) NOT NULL,
    gravedad VARCHAR(20) NOT NULL,
    descripcion TEXT NOT NULL,
    acciones_tomadas TEXT,
    usuario_id BIGINT REFERENCES usuarios(id),
    fecha_deteccion TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_eventos_donacion ON eventos_adversos_donacion(donacion_id);
