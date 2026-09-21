-- RF-11: diagnóstico CIE-10 que sustenta la solicitud transfusional.
-- Nullable a nivel de BD (por compatibilidad con solicitudes ya existentes);
-- la aplicación exige el valor para toda solicitud nueva.
ALTER TABLE solicitudes ADD COLUMN diagnostico_cie10 VARCHAR(10);
