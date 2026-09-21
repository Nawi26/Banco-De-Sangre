-- RF-41: certificado de calidad por unidad, con código de verificación (QR).
CREATE TABLE certificados_calidad (
    id BIGSERIAL PRIMARY KEY,
    hemocomponente_id BIGINT NOT NULL UNIQUE REFERENCES hemocomponentes(id),
    numero_certificado VARCHAR(60) NOT NULL UNIQUE,
    codigo_verificacion VARCHAR(60) NOT NULL UNIQUE,
    emitido_en TIMESTAMP NOT NULL DEFAULT now()
);
