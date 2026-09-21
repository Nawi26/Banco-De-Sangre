-- RF-36: historial de reportes normativos PRONAHEBAS generados automáticamente.
CREATE TABLE reportes_normativos_generados (
    id BIGSERIAL PRIMARY KEY,
    periodo_desde TIMESTAMP NOT NULL,
    periodo_hasta TIMESTAMP NOT NULL,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT now(),
    donaciones_registradas BIGINT NOT NULL,
    transfusiones_realizadas BIGINT NOT NULL,
    descartes_total BIGINT NOT NULL,
    eventos_adversos_total BIGINT NOT NULL,
    certificados_emitidos BIGINT NOT NULL
);
