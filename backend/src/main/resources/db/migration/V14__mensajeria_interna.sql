-- RF-48: canal de mensajería interna, como hilo de coordinación por solicitud.
CREATE TABLE mensajes (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes(id),
    remitente_id BIGINT NOT NULL REFERENCES usuarios(id),
    contenido TEXT NOT NULL,
    fecha_envio TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_mensajes_solicitud ON mensajes(solicitud_id);
