-- RF-25: reserva de unidades de hemocomponentes para una cirugía programada.
CREATE TABLE reservas_quirurgicas (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL REFERENCES pacientes(id),
    medico_solicitante_id BIGINT NOT NULL REFERENCES usuarios(id),
    tipo_hemocomponente VARCHAR(60) NOT NULL,
    grupo_abo VARCHAR(5) NOT NULL,
    factor_rh VARCHAR(5) NOT NULL,
    unidades_solicitadas INTEGER NOT NULL,
    fecha_cirugia_programada TIMESTAMP NOT NULL,
    horas_validez_post_cirugia INTEGER NOT NULL DEFAULT 48,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE hemocomponentes ADD COLUMN reserva_quirurgica_id BIGINT REFERENCES reservas_quirurgicas(id);
