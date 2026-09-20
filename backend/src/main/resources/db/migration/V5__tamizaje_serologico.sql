-- RF-07/RF-08: tamizaje serológico (doble digitación ciega o interfaz LIS) y su resultado de bloqueo.
CREATE TABLE tamizajes_serologicos (
    id BIGSERIAL PRIMARY KEY,
    donacion_id BIGINT NOT NULL UNIQUE REFERENCES donaciones(id),
    origen VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    usuario1_id BIGINT REFERENCES usuarios(id),
    usuario2_id BIGINT REFERENCES usuarios(id),
    fecha_primera_digitacion TIMESTAMP,
    fecha_segunda_digitacion TIMESTAMP,
    resultado_general VARCHAR(20),
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE resultados_marcador (
    id BIGSERIAL PRIMARY KEY,
    tamizaje_id BIGINT NOT NULL REFERENCES tamizajes_serologicos(id) ON DELETE CASCADE,
    marcador VARCHAR(40) NOT NULL,
    resultado_digitacion1 VARCHAR(20),
    resultado_digitacion2 VARCHAR(20),
    resultado_final VARCHAR(20),
    concordante BOOLEAN,
    UNIQUE (tamizaje_id, marcador)
);
