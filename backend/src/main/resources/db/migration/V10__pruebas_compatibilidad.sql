-- RF-12: prueba cruzada y RAI de una unidad candidata frente a una solicitud transfusional.
CREATE TABLE pruebas_compatibilidad (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes(id),
    hemocomponente_id BIGINT NOT NULL REFERENCES hemocomponentes(id),
    resultado_rai VARCHAR(30) NOT NULL,
    resultado_prueba_cruzada VARCHAR(30) NOT NULL,
    tecnologo_id BIGINT REFERENCES usuarios(id),
    fecha TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_pruebas_compatibilidad_solicitud ON pruebas_compatibilidad(solicitud_id);
CREATE INDEX idx_pruebas_compatibilidad_hemocomponente ON pruebas_compatibilidad(hemocomponente_id);
