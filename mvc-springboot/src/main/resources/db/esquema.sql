-- =====================================================================
--  Base de datos del Sistema Web de Gestión de Banco de Sangre (ISBT 128)
--  Hospital de Lima Este Vitarte
--
--  15 tablas = 12 NORMALES + 3 DE DETALLE (detalle_tamizaje, detalle_pruebas_cruzadas,
--  detalle_despachos).
--  Motor: PostgreSQL. Implementa el diseño físico documentado (15 tablas,
--  identificadores UUID, tipos ENUM, llaves foráneas sin borrado en
--  cascada). Es IDEMPOTENTE: se puede ejecutar en cada arranque sin
--  perder datos (todo es CREATE ... IF NOT EXISTS).
--
--  Diferencias con el diseño original (necesarias para que la aplicación
--  guarde todo lo que muestra en pantalla); están marcadas con "AJUSTE":
--    * estado_unidad tiene un valor más: 'fraccionado' (bolsa de sangre
--      total ya separada en sus componentes).
--    * Se agregaron columnas de texto para guardar tal cual lo que se
--      muestra (código de solicitud, historia clínica, etc.).
--    * Algunas columnas que antes eran NOT NULL ahora aceptan nulo porque
--      la aplicación todavía no las captura (fecha de nacimiento, DNI del
--      paciente, dígito verificador del DIN, donación de origen de las
--      unidades anteriores al sistema).
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------
-- 1. Tipos ENUM
-- ---------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'rol_usuario') THEN
        CREATE TYPE rol_usuario AS ENUM ('medico_solicitante','tecnologo_medico','jefe_banco_sangre','administrador');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'grupo_abo') THEN
        CREATE TYPE grupo_abo AS ENUM ('A','B','AB','O');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'factor_rh') THEN
        CREATE TYPE factor_rh AS ENUM ('positivo','negativo');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_hemocomponente') THEN
        CREATE TYPE tipo_hemocomponente AS ENUM ('sangre_total','concentrado_hematies','plasma_fresco_congelado','crioprecipitado','concentrado_plaquetas');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_unidad') THEN
        -- AJUSTE: se agrega 'fraccionado'
        CREATE TYPE estado_unidad AS ENUM ('cuarentena','disponible','reservado','bloqueado','vencido','despachado','incinerado','fraccionado');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'resultado_serologico') THEN
        CREATE TYPE resultado_serologico AS ENUM ('no_reactivo','reactivo','indeterminado');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_donacion') THEN
        CREATE TYPE tipo_donacion AS ENUM ('voluntaria','reposicion','autologa');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'prioridad_clinica') THEN
        CREATE TYPE prioridad_clinica AS ENUM ('rutina','urgente','emergencia');
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 2. Usuarios y seguridad
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id               uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    dni              varchar(15)  NOT NULL UNIQUE,
    correo           varchar(150) NOT NULL UNIQUE,
    contrasena_hash  text         NOT NULL,          -- hash bcrypt (pgcrypto), nunca la clave en claro
    nombres          varchar(150) NOT NULL,
    apellidos        varchar(150) NOT NULL,
    colegiatura      varchar(30),
    rol              rol_usuario  NOT NULL,
    activo           boolean      NOT NULL DEFAULT true,
    ultimo_acceso    timestamptz,                     -- AJUSTE: se muestra en Administración
    creado_en        timestamptz  NOT NULL DEFAULT now(),
    actualizado_en   timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS auditoria_logs (
    id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id  uuid REFERENCES usuarios(id),
    accion      varchar(300) NOT NULL,               -- AJUSTE: 100 -> 300 (las acciones se muestran completas)
    entidad     varchar(200),                        -- AJUSTE: 100 -> 200
    entidad_id  uuid,
    ip_terminal varchar(45),
    detalle     jsonb,                               -- {"usuario": "correo (Rol)"}
    creado_en   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS alertas (
    id            uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo          varchar(50)  NOT NULL,             -- color de la alerta: red, amber, blue, purple
    referencia_id uuid,
    titulo        varchar(200) NOT NULL,             -- AJUSTE: título corto de la alerta
    mensaje       text         NOT NULL,             -- detalle
    atendida      boolean      NOT NULL DEFAULT false,
    creado_en     timestamptz  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 3. Donantes (incluye sus donaciones: total, última y tipo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS donantes (
    id                     uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    dni                    varchar(15)  NOT NULL UNIQUE,
    nombres                varchar(150) NOT NULL,
    apellidos              varchar(150) NOT NULL,
    fecha_nacimiento       date         NOT NULL,
    sexo                   char(1)      NOT NULL CHECK (sexo IN ('M','F')),
    grupo_abo              grupo_abo,                -- nulo solo mientras el laboratorio no confirma el grupo
    factor_rh              factor_rh,
    peso_kg                numeric(5,2) NOT NULL,
    hemoglobina_gdl        numeric(4,1) NOT NULL,
    telefono               varchar(9)   NOT NULL,
    correo                 varchar(150) NOT NULL,
    direccion              text         NOT NULL,
    antecedentes_viaje     text         NOT NULL DEFAULT 'Ninguno',
    campana                varchar(100) NOT NULL DEFAULT 'Donación directa',
    apto                   boolean      NOT NULL,
    diferido               boolean NOT NULL DEFAULT false,
    motivo_diferimiento    text,                     -- solo si está diferido (ver ck_diferido_con_motivo)
    diferimiento_hasta     date,                     -- solo en diferimientos temporales con fecha
    consentimiento_firmado boolean NOT NULL DEFAULT false,
    proxima_cita_en        timestamptz,
    creado_en              timestamptz NOT NULL DEFAULT now(),
    tipo_donacion          tipo_donacion NOT NULL DEFAULT 'voluntaria',
    total_donaciones       integer NOT NULL DEFAULT 0,
    ultima_donacion        date,                     -- nulo hasta la primera donación
    diferimiento_tipo      varchar(15),              -- 'Temporal' o 'Permanente'
    CONSTRAINT ck_grupo_completo CHECK ((grupo_abo IS NULL) = (factor_rh IS NULL))
);


-- ---------------------------------------------------------------------
-- 4. Unidades, hemocomponentes, tamizaje y cámaras
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS unidades_sangre (
    id                uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    donante_id        uuid REFERENCES donantes(id),          -- donante de origen (donantes y donaciones se fusionan en una sola tabla); nulo en unidades anteriores al sistema
    responsable_id    uuid REFERENCES usuarios(id),          -- quién registró la extracción
    din               varchar(20) NOT NULL UNIQUE,           -- Donation Identification Number (ISBT 128)
    din_check_digit   varchar(2),                            -- AJUSTE: acepta nulo
    grupo_abo         grupo_abo NOT NULL,
    factor_rh         factor_rh NOT NULL,
    fecha_extraccion  timestamptz NOT NULL DEFAULT now(),
    fecha_vencimiento date NOT NULL,
    estado            estado_unidad NOT NULL DEFAULT 'cuarentena',
    creado_en         timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS camaras_refrigeracion (
    id                  uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre              varchar(100) NOT NULL,
    etiqueta            varchar(50)  NOT NULL UNIQUE,        -- AJUSTE: nombre corto que se ve en Inventario
    tipo                varchar(30)  NOT NULL,
    temp_min            numeric(4,1) NOT NULL,
    temp_max            numeric(4,1) NOT NULL,
    ultima_temperatura  numeric(4,1),
    ultima_lectura_en   timestamptz,
    fuera_de_rango      boolean NOT NULL DEFAULT false,
    ultima_calibracion  date
);

-- TABLA DE DETALLE 1 de 3 (detalle de la unidad de sangre: un tamizaje por unidad)
CREATE TABLE IF NOT EXISTS detalle_tamizaje (
    id               uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    unidad_matriz_id uuid NOT NULL UNIQUE REFERENCES unidades_sangre(id),   -- AJUSTE: un tamizaje vigente por bolsa
    vih              resultado_serologico NOT NULL,
    hepatitis_b      resultado_serologico NOT NULL,
    hepatitis_c      resultado_serologico NOT NULL,
    sifilis          resultado_serologico NOT NULL,
    chagas           resultado_serologico NOT NULL,
    htlv             resultado_serologico NOT NULL,
    doble_digitacion boolean NOT NULL DEFAULT true,   -- solo se guarda si las dos digitaciones coinciden
    registrado_por   uuid NOT NULL REFERENCES usuarios(id),
    creado_en        timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS hemocomponentes (
    id                uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    unidad_matriz_id  uuid NOT NULL REFERENCES unidades_sangre(id),
    codigo_producto   varchar(20) NOT NULL,
    din_derivado      varchar(30) NOT NULL UNIQUE,           -- AJUSTE: 20 -> 30
    tipo              tipo_hemocomponente NOT NULL,
    volumen_ml        numeric(6,2),
    fecha_vencimiento date NOT NULL,
    estado            estado_unidad NOT NULL DEFAULT 'cuarentena',
    camara_id         uuid REFERENCES camaras_refrigeracion(id),
    motivo_descarte   varchar(50),
    descartado_por    uuid REFERENCES usuarios(id),
    descartado_en     timestamptz,
    creado_en         timestamptz NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 5. Solicitudes, pruebas cruzadas, detalle_despachos y hemovigilancia
-- ---------------------------------------------------------------------
-- Pacientes: un registro por historia clínica; las solicitudes lo referencian (no repiten nombre ni DNI)
CREATE TABLE IF NOT EXISTS pacientes (
    id                uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    dni               varchar(8) NOT NULL UNIQUE,
    historia_clinica  varchar(30) NOT NULL UNIQUE,
    nombres           varchar(150) NOT NULL,
    fecha_nacimiento  date         NOT NULL,
    sexo              char(1)      NOT NULL CHECK (sexo IN ('M','F')),
    grupo_abo         grupo_abo,                -- nulo solo mientras el laboratorio no confirma el grupo
    factor_rh         factor_rh,
    peso_kg           numeric(5,2) NOT NULL,
    hemoglobina_gdl   numeric(4,1) NOT NULL,
    telefono          varchar(9)   NOT NULL,
    correo            varchar(150) NOT NULL,
    direccion         text         NOT NULL,
    creado_en         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_paciente_grupo_completo CHECK ((grupo_abo IS NULL) = (factor_rh IS NULL))
);

CREATE TABLE IF NOT EXISTS solicitudes_transfusionales (
    id                        uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo                    varchar(20) NOT NULL UNIQUE,   -- AJUSTE: SOL-2026-0341
    medico_id                 uuid NOT NULL REFERENCES usuarios(id),
    servicio                  varchar(60) NOT NULL,
    paciente_id               uuid NOT NULL REFERENCES pacientes(id),
    diagnostico_cie10         varchar(10) NOT NULL,
    diagnostico_texto         text NOT NULL,                        -- AJUSTE: "K92.2 — Hemorragia digestiva alta"
    componente                text NOT NULL,                        -- AJUSTE: "2 unidades — Concentrado de Hematíes"
    prioridad                 prioridad_clinica NOT NULL,
    prioridad_texto           varchar(30) NOT NULL,                 -- AJUSTE: "Urgente (<15 min)"
    tipo_solicitud            varchar(20) NOT NULL DEFAULT 'transfusional',
    fecha_liberacion_reserva  timestamptz,
    reserva_texto             varchar(60),                   -- AJUSTE: fecha límite tal como se escribió
    estado                    varchar(20) NOT NULL DEFAULT 'pendiente',   -- AJUSTE: pendiente | compatible | incompatible
    creado_en                 timestamptz NOT NULL DEFAULT now()
);

-- TABLA DE DETALLE 2 de 3 (detalle de la solicitud transfusional: pruebas de compatibilidad)
CREATE TABLE IF NOT EXISTS detalle_pruebas_cruzadas (
    id                   uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    solicitud_id         uuid NOT NULL UNIQUE REFERENCES solicitudes_transfusionales(id),   -- AJUSTE: última tanda de pruebas
    hemocomponente_id    uuid NOT NULL REFERENCES hemocomponentes(id),
    prueba_mayor         boolean NOT NULL,                   -- true = sin aglutinación
    prueba_menor         boolean NOT NULL,
    rastreo_anticuerpos  boolean NOT NULL,                   -- true = RAI negativo
    compatible           boolean NOT NULL,
    validado_por         uuid NOT NULL REFERENCES usuarios(id),
    creado_en            timestamptz NOT NULL DEFAULT now(),
    -- AJUSTE: resultado exacto de cada prueba
    muestra              varchar(30) NOT NULL,
    grupo_paciente       varchar(10) NOT NULL,
    resultado_mayor      varchar(30) NOT NULL,
    resultado_menor      varchar(30) NOT NULL,
    resultado_rai        varchar(30) NOT NULL
);

-- TABLA DE DETALLE 3 de 3 (detalle de la solicitud: entrega del hemocomponente al paciente)
CREATE TABLE IF NOT EXISTS detalle_despachos (
    id                        uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    solicitud_id              uuid REFERENCES solicitudes_transfusionales(id),   -- AJUSTE: acepta nulo (pulsera sin solicitud registrada)
    hemocomponente_id         uuid NOT NULL REFERENCES hemocomponentes(id),
    escaneo_hemocomponente    boolean NOT NULL,
    escaneo_pulsera_receptor  boolean NOT NULL,
    autorizado_por            uuid NOT NULL REFERENCES usuarios(id),
    fecha_despacho            timestamptz NOT NULL DEFAULT now(),
    historia_clinica          varchar(30)                    -- AJUSTE: HC leída de la pulsera
);

CREATE TABLE IF NOT EXISTS eventos_hemovigilancia (
    id                uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    hemocomponente_id uuid REFERENCES hemocomponentes(id),
    donante_id        uuid REFERENCES donantes(id),
    tipo_evento       varchar(100) NOT NULL,
    momento           varchar(20)  NOT NULL DEFAULT 'post-transfusional',
    descripcion       text,
    reportado_por     uuid NOT NULL REFERENCES usuarios(id),
    creado_en         timestamptz NOT NULL DEFAULT now(),
    -- AJUSTE: datos que muestra la pantalla
    paciente          varchar(150) NOT NULL,
    din_texto         varchar(30) NOT NULL,
    severidad         varchar(30) NOT NULL DEFAULT 'amber:Leve',
    estado            varchar(40) NOT NULL DEFAULT 'amber:En investigación'
);

-- ---------------------------------------------------------------------
-- 6. Red interhospitalaria
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS establecimientos_red (
    id                      uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre                  varchar(150) NOT NULL UNIQUE,
    red                     varchar(100),
    convenio_descripcion    text,
    convenio_vigente_desde  date,
    convenio_vigente_hasta  date,
    es_sede_local           boolean NOT NULL DEFAULT false    -- AJUSTE: el propio hospital
);

CREATE TABLE IF NOT EXISTS solicitudes_interhospitalarias (
    id                         uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    establecimiento_origen_id  uuid NOT NULL REFERENCES establecimientos_red(id),
    establecimiento_destino_id uuid NOT NULL REFERENCES establecimientos_red(id),
    grupo_abo                  grupo_abo NOT NULL,
    factor_rh                  factor_rh NOT NULL,
    tipo_hemocomponente        tipo_hemocomponente NOT NULL,
    cantidad                   integer NOT NULL CHECK (cantidad > 0),
    estado                     varchar(30) NOT NULL DEFAULT 'pendiente',   -- pendiente | aceptada | devuelta
    es_devolucion              boolean NOT NULL DEFAULT false,
    creado_en                  timestamptz NOT NULL DEFAULT now(),
    respondido_en              timestamptz,
    motivo                     text                                        -- AJUSTE
);

-- ---------------------------------------------------------------------
-- 7. Índices (PostgreSQL no indexa solas las llaves foráneas)
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_hemocomp_estado        ON hemocomponentes (estado);
CREATE INDEX IF NOT EXISTS idx_hemocomp_vencimiento   ON hemocomponentes (fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_hemocomp_matriz        ON hemocomponentes (unidad_matriz_id);
CREATE INDEX IF NOT EXISTS idx_unidades_estado        ON unidades_sangre (estado);
CREATE INDEX IF NOT EXISTS idx_unidades_donante       ON unidades_sangre (donante_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_prioridad  ON solicitudes_transfusionales (prioridad, creado_en);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha        ON auditoria_logs (creado_en DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_entidad      ON auditoria_logs (entidad_id);
CREATE INDEX IF NOT EXISTS idx_hemovig_tipo           ON eventos_hemovigilancia (tipo_evento);
CREATE INDEX IF NOT EXISTS idx_despachos_hemocomp     ON detalle_despachos (hemocomponente_id);
CREATE INDEX IF NOT EXISTS idx_interhosp_origen       ON solicitudes_interhospitalarias (establecimiento_origen_id);
CREATE INDEX IF NOT EXISTS idx_interhosp_destino      ON solicitudes_interhospitalarias (establecimiento_destino_id);

-- ---------------------------------------------------------------------
-- Restricciones para que no entren datos vacíos
--   * NOT NULL en las columnas que siempre se llenan (en bases ya creadas se aplican con ALTER).
--   * CHECK en las columnas que solo se llenan en ciertos casos
--     (una reserva o una solicitud programada debe traer su fecha; un donante diferido, su motivo y su tipo; un diferimiento temporal, la fecha en que termina).
-- ---------------------------------------------------------------------
DO $$
BEGIN
    -- Bases ya creadas: se agregan las columnas nuevas de pacientes (aceptan nulo solo en filas antiguas)
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS fecha_nacimiento date;
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS sexo char(1);
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS grupo_abo grupo_abo;
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS factor_rh factor_rh;
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS peso_kg numeric(5,2);
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS hemoglobina_gdl numeric(4,1);
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS telefono varchar(9);
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS correo varchar(150);
    ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS direccion text;
    -- Donantes ya guardados: se llenan los datos que quedaron vacíos
    UPDATE donantes SET diferimiento_tipo = 'Temporal'
     WHERE diferido AND diferimiento_tipo IS NULL;
    UPDATE donantes SET motivo_diferimiento = 'Sin motivo registrado'
     WHERE diferido AND motivo_diferimiento IS NULL;
    UPDATE donantes SET diferimiento_hasta = (creado_en + interval '30 days')::date
     WHERE diferido AND diferimiento_tipo = 'Temporal' AND diferimiento_hasta IS NULL;
    UPDATE donantes SET antecedentes_viaje = coalesce(motivo_diferimiento, 'Ninguno')
     WHERE antecedentes_viaje IS NULL OR btrim(antecedentes_viaje) = '';
    ALTER TABLE donantes ALTER COLUMN antecedentes_viaje SET DEFAULT 'Ninguno';
    ALTER TABLE donantes ALTER COLUMN antecedentes_viaje SET NOT NULL;
    ALTER TABLE pacientes                  ALTER COLUMN dni SET NOT NULL;
    ALTER TABLE solicitudes_transfusionales ALTER COLUMN diagnostico_texto SET NOT NULL;
    ALTER TABLE solicitudes_transfusionales ALTER COLUMN componente SET NOT NULL;
    ALTER TABLE solicitudes_transfusionales ALTER COLUMN prioridad_texto SET NOT NULL;
    ALTER TABLE detalle_pruebas_cruzadas   ALTER COLUMN muestra SET NOT NULL;
    ALTER TABLE detalle_pruebas_cruzadas   ALTER COLUMN grupo_paciente SET NOT NULL;
    ALTER TABLE detalle_pruebas_cruzadas   ALTER COLUMN resultado_mayor SET NOT NULL;
    ALTER TABLE detalle_pruebas_cruzadas   ALTER COLUMN resultado_menor SET NOT NULL;
    ALTER TABLE detalle_pruebas_cruzadas   ALTER COLUMN resultado_rai SET NOT NULL;
    ALTER TABLE eventos_hemovigilancia     ALTER COLUMN paciente SET NOT NULL;
    ALTER TABLE eventos_hemovigilancia     ALTER COLUMN din_texto SET NOT NULL;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_reserva_con_fecha') THEN
        ALTER TABLE solicitudes_transfusionales ADD CONSTRAINT ck_reserva_con_fecha
            CHECK (tipo_solicitud <> 'reserva' OR reserva_texto IS NOT NULL);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_programada_con_fecha') THEN
        ALTER TABLE solicitudes_transfusionales ADD CONSTRAINT ck_programada_con_fecha
            CHECK (prioridad_texto <> 'Programada' OR reserva_texto IS NOT NULL) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_temporal_con_fecha') THEN
        ALTER TABLE donantes ADD CONSTRAINT ck_temporal_con_fecha
            CHECK (NOT diferido OR diferimiento_tipo <> 'Temporal' OR diferimiento_hasta IS NOT NULL) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_paciente_grupo_completo') THEN
        ALTER TABLE pacientes ADD CONSTRAINT ck_paciente_grupo_completo
            CHECK ((grupo_abo IS NULL) = (factor_rh IS NULL));
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_diferido_con_motivo') THEN
        ALTER TABLE donantes ADD CONSTRAINT ck_diferido_con_motivo
            CHECK (NOT diferido OR (motivo_diferimiento IS NOT NULL AND diferimiento_tipo IS NOT NULL));
    END IF;
END
$$;
