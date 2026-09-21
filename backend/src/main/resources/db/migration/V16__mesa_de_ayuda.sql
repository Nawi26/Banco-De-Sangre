-- RF-33: mesa de ayuda interna para incidencias técnicas.
CREATE TABLE tickets_soporte (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    asunto VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    prioridad VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    respuesta TEXT,
    resuelto_por_id BIGINT REFERENCES usuarios(id),
    creado_en TIMESTAMP NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMP NOT NULL DEFAULT now()
);
