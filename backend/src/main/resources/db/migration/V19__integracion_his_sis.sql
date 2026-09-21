-- RF-32: trazabilidad e idempotencia de solicitudes importadas automáticamente desde el HIS/SIS.
ALTER TABLE solicitudes ADD COLUMN codigo_orden_externa VARCHAR(60) UNIQUE;
