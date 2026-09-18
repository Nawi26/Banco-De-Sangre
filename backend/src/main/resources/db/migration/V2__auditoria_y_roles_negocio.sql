-- RF-02: estandariza los 4 roles de negocio (Médico Solicitante, Tecnólogo Médico,
-- Jefe de Banco de Sangre, Administrador) sin tocar roles/usuarios ya existentes.
DO $$
DECLARE
    siguiente_id BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'Administrador') THEN
        SELECT COALESCE(MAX(id), 0) + 1 INTO siguiente_id FROM roles;
        INSERT INTO roles (id, nombre, descripcion)
        VALUES (siguiente_id, 'Administrador', 'Administra usuarios, roles y configuración del sistema');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'Médico Solicitante') THEN
        SELECT COALESCE(MAX(id), 0) + 1 INTO siguiente_id FROM roles;
        INSERT INTO roles (id, nombre, descripcion)
        VALUES (siguiente_id, 'Médico Solicitante', 'Registra solicitudes transfusionales para sus pacientes');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'Tecnólogo Médico') THEN
        SELECT COALESCE(MAX(id), 0) + 1 INTO siguiente_id FROM roles;
        INSERT INTO roles (id, nombre, descripcion)
        VALUES (siguiente_id, 'Tecnólogo Médico', 'Ejecuta tamizaje, fraccionamiento, inventario y despacho de hemocomponentes');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'Jefe de Banco de Sangre') THEN
        SELECT COALESCE(MAX(id), 0) + 1 INTO siguiente_id FROM roles;
        INSERT INTO roles (id, nombre, descripcion)
        VALUES (siguiente_id, 'Jefe de Banco de Sangre', 'Supervisa y aprueba los procesos críticos del banco de sangre');
    END IF;
END $$;

-- RF-17 / RNF-04: bitácora de auditoría inmutable (sólo inserción, sin update/delete a nivel de aplicación).
CREATE TABLE auditoria_log (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuarios(id),
    usuario_identificador VARCHAR(150),
    rol VARCHAR(100),
    metodo_http VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    accion VARCHAR(100),
    ip_origen VARCHAR(64),
    resultado_http INTEGER,
    exitoso BOOLEAN NOT NULL DEFAULT TRUE,
    detalle VARCHAR(500),
    creado_en TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_usuario ON auditoria_log(usuario_id);
CREATE INDEX idx_auditoria_creado_en ON auditoria_log(creado_en);
