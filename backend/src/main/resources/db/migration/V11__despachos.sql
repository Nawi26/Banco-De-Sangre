-- RF-13: doble verificación electrónica del despacho de una unidad hacia una solicitud.
CREATE TABLE despachos (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes(id),
    hemocomponente_id BIGINT NOT NULL UNIQUE REFERENCES hemocomponentes(id),
    primera_verificacion_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    primera_verificacion_en TIMESTAMP NOT NULL,
    segunda_verificacion_usuario_id BIGINT REFERENCES usuarios(id),
    segunda_verificacion_en TIMESTAMP,
    estado VARCHAR(30) NOT NULL,
    fecha_despacho TIMESTAMP
);

CREATE INDEX idx_despachos_solicitud ON despachos(solicitud_id);
