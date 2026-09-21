-- RF-09/RF-24: cámaras de refrigeración/congelación y su monitoreo de temperatura.
CREATE TABLE camaras_almacenamiento (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    ubicacion VARCHAR(150),
    temperatura_minima NUMERIC(5,2) NOT NULL,
    temperatura_maxima NUMERIC(5,2) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE registros_temperatura (
    id BIGSERIAL PRIMARY KEY,
    camara_id BIGINT NOT NULL REFERENCES camaras_almacenamiento(id),
    temperatura NUMERIC(5,2) NOT NULL,
    dentro_de_rango BOOLEAN NOT NULL,
    usuario_id BIGINT REFERENCES usuarios(id),
    registrado_en TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_registros_temperatura_camara ON registros_temperatura(camara_id, registrado_en DESC);

ALTER TABLE hemocomponentes ADD COLUMN camara_id BIGINT REFERENCES camaras_almacenamiento(id);
