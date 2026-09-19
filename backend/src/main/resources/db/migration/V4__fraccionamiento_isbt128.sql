-- RF-06: trazabilidad del hemocomponente hacia su DIN matriz (donación de origen).
ALTER TABLE hemocomponentes ADD COLUMN donacion_id BIGINT REFERENCES donaciones(id);
CREATE INDEX idx_hemocomponentes_donacion ON hemocomponentes(donacion_id);
