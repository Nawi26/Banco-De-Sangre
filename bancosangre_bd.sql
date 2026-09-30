-- =====================================================================
-- Base de datos: Sistema Web de Gestión de Banco de Sangre (ISBT 128) - HLEV
-- PostgreSQL 14 o superior. Script único: esquema + funciones + datos de ejemplo.
--
-- Uso:
--   1) Como administrador:  CREATE DATABASE bancosangre;
--   2) psql -U postgres -d bancosangre -f bancosangre_bd.sql
-- Se puede ejecutar varias veces sin duplicar datos.
-- Cuentas del equipo (correos @hlev.gob.pe): solo se guarda el hash de cada contraseña.
-- =====================================================================

-- ################ esquema.sql ################
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

-- ################ funciones.sql ################
-- =====================================================================
--  Funciones de acceso a datos.
--
--  La aplicación web trabaja con listas de objetos JSON (donantes,
--  inventario, solicitudes...). Estas funciones convierten esas listas
--  a las tablas normalizadas y viceversa, dentro de una transacción:
--    api_listar(coleccion)                 -> jsonb (lo que ve la pantalla)
--    api_sincronizar(cambios, correo, rol) -> guarda lo que cambió
--  Son IDEMPOTENTES (CREATE OR REPLACE).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Conversiones entre el texto de la pantalla y los ENUM
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION rol_a_enum(p text) RETURNS rol_usuario LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE p
        WHEN 'Médico Solicitante'        THEN 'medico_solicitante'::rol_usuario
        WHEN 'Tecnólogo Médico'          THEN 'tecnologo_medico'::rol_usuario
        WHEN 'Jefe de Banco de Sangre'   THEN 'jefe_banco_sangre'::rol_usuario
        WHEN 'Administrador'             THEN 'administrador'::rol_usuario
    END
$$;

CREATE OR REPLACE FUNCTION rol_a_texto(p rol_usuario) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE p
        WHEN 'medico_solicitante' THEN 'Médico Solicitante'
        WHEN 'tecnologo_medico'   THEN 'Tecnólogo Médico'
        WHEN 'jefe_banco_sangre'  THEN 'Jefe de Banco de Sangre'
        WHEN 'administrador'      THEN 'Administrador'
    END
$$;

-- "O NEG" -> grupo O / negativo ; devuelve NULL si el texto no es un grupo válido
CREATE OR REPLACE FUNCTION abo_de_texto(p text) RETURNS grupo_abo LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE WHEN upper(split_part(trim(coalesce(p,'')),' ',1)) IN ('A','B','AB','O')
                THEN upper(split_part(trim(p),' ',1))::grupo_abo END
$$;

CREATE OR REPLACE FUNCTION rh_de_texto(p text) RETURNS factor_rh LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE upper(split_part(trim(coalesce(p,'')),' ',2))
        WHEN 'POS' THEN 'positivo'::factor_rh
        WHEN 'NEG' THEN 'negativo'::factor_rh
    END
$$;

CREATE OR REPLACE FUNCTION grupo_a_texto(a grupo_abo, r factor_rh) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE WHEN a IS NULL OR r IS NULL THEN 'Por confirmar'
                ELSE a::text || ' ' || CASE r WHEN 'positivo' THEN 'POS' ELSE 'NEG' END END
$$;

CREATE OR REPLACE FUNCTION producto_a_tipo(p text) RETURNS tipo_hemocomponente LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE p
        WHEN 'Sangre Total'              THEN 'sangre_total'::tipo_hemocomponente
        WHEN 'Concentrado de Hematíes'   THEN 'concentrado_hematies'
        WHEN 'Plasma Fresco Congelado'   THEN 'plasma_fresco_congelado'
        WHEN 'Crioprecipitado'           THEN 'crioprecipitado'
        WHEN 'Concentrado de Plaquetas'  THEN 'concentrado_plaquetas'
    END
$$;

CREATE OR REPLACE FUNCTION tipo_a_producto(t tipo_hemocomponente) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE t
        WHEN 'sangre_total'             THEN 'Sangre Total'
        WHEN 'concentrado_hematies'     THEN 'Concentrado de Hematíes'
        WHEN 'plasma_fresco_congelado'  THEN 'Plasma Fresco Congelado'
        WHEN 'crioprecipitado'          THEN 'Crioprecipitado'
        WHEN 'concentrado_plaquetas'    THEN 'Concentrado de Plaquetas'
    END
$$;

CREATE OR REPLACE FUNCTION tipo_a_codigo(t tipo_hemocomponente) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE t
        WHEN 'sangre_total'             THEN 'ST'
        WHEN 'concentrado_hematies'     THEN 'CH'
        WHEN 'plasma_fresco_congelado'  THEN 'PFC'
        WHEN 'crioprecipitado'          THEN 'CRIO'
        WHEN 'concentrado_plaquetas'    THEN 'CP'
    END
$$;

CREATE OR REPLACE FUNCTION tipo_donacion_de_texto(p text) RETURNS tipo_donacion LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE lower(coalesce(p,''))
        WHEN 'reposición' THEN 'reposicion'::tipo_donacion
        WHEN 'reposicion' THEN 'reposicion'
        WHEN 'autóloga'   THEN 'autologa'
        WHEN 'autologa'   THEN 'autologa'
        ELSE 'voluntaria'
    END
$$;

CREATE OR REPLACE FUNCTION tipo_donacion_a_texto(t tipo_donacion) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE t WHEN 'voluntaria' THEN 'Voluntaria' WHEN 'reposicion' THEN 'Reposición' ELSE 'Autóloga' END
$$;

CREATE OR REPLACE FUNCTION resultado_de_texto(p text) RETURNS resultado_serologico LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE p
        WHEN 'Reactivo'       THEN 'reactivo'::resultado_serologico
        WHEN 'Indeterminado'  THEN 'indeterminado'
        ELSE 'no_reactivo'
    END
$$;

CREATE OR REPLACE FUNCTION resultado_a_texto(r resultado_serologico) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE r WHEN 'reactivo' THEN 'Reactivo' WHEN 'indeterminado' THEN 'Indeterminado' ELSE 'No reactivo' END
$$;

-- Fechas: la pantalla usa dd/mm/aaaa y la hora de Lima
CREATE OR REPLACE FUNCTION fecha_texto(p date) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE WHEN p IS NULL THEN '—' ELSE to_char(p, 'DD/MM/YYYY') END
$$;

CREATE OR REPLACE FUNCTION fecha_hora_texto(p timestamptz, con_segundos boolean DEFAULT false) RETURNS text LANGUAGE sql STABLE AS $$
    SELECT to_char(p AT TIME ZONE 'America/Lima', CASE WHEN con_segundos THEN 'DD/MM/YYYY HH24:MI:SS' ELSE 'DD/MM/YYYY HH24:MI' END)
$$;

-- "12/09/2026" -> date ; cualquier otra cosa ("—", "Sin fecha límite") -> NULL
CREATE OR REPLACE FUNCTION texto_a_fecha(p text) RETURNS date LANGUAGE plpgsql IMMUTABLE AS $$
BEGIN
    IF p ~ '^\d{2}/\d{2}/\d{4}' THEN
        RETURN to_date(substr(p, 1, 10), 'DD/MM/YYYY');
    ELSIF p ~ '^\d{2}/\d{4}$' THEN
        -- "09/2027": vence el último día de ese mes
        RETURN (to_date(p, 'MM/YYYY') + interval '1 month - 1 day')::date;
    END IF;
    RETURN NULL;
EXCEPTION WHEN others THEN
    RETURN NULL;
END
$$;

-- "Apellidos, Nombres" -> apellidos / nombres (sin coma: todo va a nombres)
CREATE OR REPLACE FUNCTION apellidos_de(p text) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE WHEN position(',' in coalesce(p,'')) > 0 THEN trim(substr(p, 1, position(',' in p) - 1)) ELSE '' END
$$;
CREATE OR REPLACE FUNCTION nombres_de(p text) RETURNS text LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE WHEN position(',' in coalesce(p,'')) > 0 THEN trim(substr(p, position(',' in p) + 1)) ELSE trim(coalesce(p,'')) END
$$;

-- ---------------------------------------------------------------------
-- Quién realiza la acción: la cuenta que coincide con el correo o DNI de
-- la sesión; si no existe, la primera cuenta activa con ese rol.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION actor_id(p_correo text, p_rol text) RETURNS uuid LANGUAGE plpgsql STABLE AS $$
DECLARE v uuid;
BEGIN
    SELECT id INTO v FROM usuarios
     WHERE lower(correo) = lower(coalesce(p_correo,'')) OR dni = coalesce(p_correo,'') LIMIT 1;
    IF v IS NULL THEN
        SELECT id INTO v FROM usuarios WHERE rol = rol_a_enum(p_rol) AND activo ORDER BY creado_en LIMIT 1;
    END IF;
    IF v IS NULL THEN
        SELECT id INTO v FROM usuarios WHERE activo ORDER BY creado_en LIMIT 1;
    END IF;
    IF v IS NULL THEN
        RAISE EXCEPTION 'No hay ningún usuario registrado para asignar la acción';
    END IF;
    RETURN v;
END
$$;

-- "Apellidos, Nombres (Rol)" de un usuario, tal como se muestra en pantalla
CREATE OR REPLACE FUNCTION usuario_texto(p_id uuid) RETURNS text LANGUAGE sql STABLE AS $$
    SELECT apellidos || ', ' || nombres || ' (' || rol_a_texto(rol) || ')' FROM usuarios WHERE id = p_id
$$;

-- ---------------------------------------------------------------------
-- LECTURA: api_listar(coleccion) -> jsonb
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION api_listar(p_coleccion text) RETURNS jsonb LANGUAGE plpgsql STABLE AS $$
DECLARE r jsonb;
BEGIN
    CASE p_coleccion

    WHEN 'usuarios' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'nombre', CASE WHEN u.apellidos = '' THEN u.nombres ELSE u.apellidos || ', ' || u.nombres END,
                   'rol', rol_a_texto(u.rol),
                   'ultimo', coalesce(fecha_hora_texto(u.ultimo_acceso), 'Sin ingresar aún'),
                   'activo', u.activo,
                   'dni', u.dni,
                   'correo', u.correo) ORDER BY u.creado_en DESC, u.apellidos), '[]'::jsonb)
          INTO r FROM usuarios u;

    WHEN 'donantes' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'nombre', d.apellidos || CASE WHEN d.nombres <> '' THEN ', ' || d.nombres ELSE '' END,
                   'dni', d.dni,
                   'abo', grupo_a_texto(d.grupo_abo, d.factor_rh),
                   'tipo', tipo_donacion_a_texto(d.tipo_donacion),
                   'ultimaDonacion', fecha_texto(d.ultima_donacion),
                   'donaciones', d.total_donaciones,
                   'estado', CASE WHEN d.diferido AND d.diferimiento_tipo = 'Permanente' THEN 'red:Excluido'
                                  WHEN d.diferido THEN 'amber:Diferido'
                                  ELSE 'green:Apto' END,
                   'motivoDiferimiento', d.motivo_diferimiento,
                   'tipoDiferimiento', d.diferimiento_tipo,
                   'vigenciaDiferimiento', fecha_texto(d.diferimiento_hasta),
                   'sexo', d.sexo, 'telefono', d.telefono, 'correo', d.correo, 'direccion', d.direccion,
                   'fechaNacimiento', fecha_texto(d.fecha_nacimiento), 'pesoKg', d.peso_kg,
                   'hemoglobina', d.hemoglobina_gdl, 'antecedentes', d.antecedentes_viaje,
                   'campana', d.campana, 'consentimiento', d.consentimiento_firmado
               ) ORDER BY d.creado_en DESC, d.apellidos), '[]'::jsonb)
          INTO r FROM donantes d;

    WHEN 'inventario' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'din', h.din_derivado,
                   'producto', tipo_a_producto(h.tipo),
                   'abo', grupo_a_texto(u.grupo_abo, u.factor_rh),
                   'camara', coalesce(c.etiqueta, '—'),
                   'exp', fecha_texto(h.fecha_vencimiento),
                   'status', h.estado::text,
                   'donante', coalesce(dn.dni, '—')
               ) ORDER BY h.fecha_vencimiento, h.din_derivado), '[]'::jsonb)
          INTO r
          FROM hemocomponentes h
          JOIN unidades_sangre u ON u.id = h.unidad_matriz_id
          LEFT JOIN camaras_refrigeracion c ON c.id = h.camara_id
          LEFT JOIN donantes dn ON dn.id = u.donante_id;

    WHEN 'temperaturas' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'camara', c.nombre,
                   'rango', trim_scale(c.temp_min)::text || '°C' || CASE WHEN c.temp_min < 0 THEN ' a ' ELSE ' – ' END || trim_scale(c.temp_max)::text || '°C',
                   'actual', c.ultima_temperatura,
                   'ok', NOT c.fuera_de_rango
               ) ORDER BY (c.tipo = 'congelacion'), c.nombre), '[]'::jsonb)
          INTO r FROM camaras_refrigeracion c WHERE c.ultima_temperatura IS NOT NULL;

    WHEN 'serologia' THEN
        -- Una fila por marcador, como la muestra la pantalla; las dos digitaciones son iguales
        -- porque solo se guarda el tamizaje cuando coinciden.
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'din', u.din, 'marcador', m.marcador, 'dig1', m.res, 'dig2', m.res, 'resultado', m.res,
                   'digitador', usuario_texto(t.registrado_por),
                   'fecha', fecha_hora_texto(t.creado_en)
               ) ORDER BY t.creado_en, u.din, m.orden), '[]'::jsonb)
          INTO r
          FROM detalle_tamizaje t
          JOIN unidades_sangre u ON u.id = t.unidad_matriz_id
          CROSS JOIN LATERAL (VALUES
              (1, 'VIH 1/2',              resultado_a_texto(t.vih)),
              (2, 'Hepatitis B (HBsAg)',  resultado_a_texto(t.hepatitis_b)),
              (3, 'Hepatitis C',          resultado_a_texto(t.hepatitis_c)),
              (4, 'Sífilis (VDRL/RPR)',   resultado_a_texto(t.sifilis)),
              (5, 'Chagas',               resultado_a_texto(t.chagas)),
              (6, 'HTLV I/II',            resultado_a_texto(t.htlv))
          ) AS m(orden, marcador, res);

    WHEN 'pacientes' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'nombre', pa.nombres, 'dni', coalesce(pa.dni, ''), 'hc', pa.historia_clinica,
                   'fechaNacimiento', fecha_texto(pa.fecha_nacimiento), 'sexo', pa.sexo,
                   'abo', grupo_a_texto(pa.grupo_abo, pa.factor_rh), 'pesoKg', pa.peso_kg,
                   'hemoglobina', pa.hemoglobina_gdl, 'telefono', pa.telefono, 'correo', pa.correo,
                   'direccion', pa.direccion,
                   'solicitudes', coalesce(s.total, 0),
                   'ultima', coalesce(fecha_texto((s.ultima AT TIME ZONE 'America/Lima')::date), '—'),
                   'estado', coalesce(s.estado, '—')
               ) ORDER BY pa.nombres), '[]'::jsonb)
          INTO r
          FROM pacientes pa
          LEFT JOIN LATERAL (SELECT count(*) AS total, max(x.creado_en) AS ultima,
                                    (array_agg(x.estado ORDER BY x.creado_en DESC))[1] AS estado
                               FROM solicitudes_transfusionales x WHERE x.paciente_id = pa.id) s ON true;

    WHEN 'solicitudes' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'id', s.codigo, 'paciente', pa.nombres, 'dni', pa.dni, 'hc', pa.historia_clinica,
                   'servicio', s.servicio, 'cie10', coalesce(s.diagnostico_texto, s.diagnostico_cie10),
                   'componente', s.componente, 'prioridad', coalesce(s.prioridad_texto, s.prioridad::text),
                   'tipo', s.tipo_solicitud,
                   'reservaFecha', coalesce(s.reserva_texto, fecha_texto(s.fecha_liberacion_reserva::date)),
                   'estado', s.estado
               ) ORDER BY s.creado_en DESC, s.codigo DESC), '[]'::jsonb)
          INTO r FROM solicitudes_transfusionales s JOIN pacientes pa ON pa.id = s.paciente_id;

    WHEN 'pruebas' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'solicitudId', s.codigo, 'prueba', x.prueba, 'muestra', p.muestra, 'unidad', h.din_derivado,
                   'grupoPaciente', p.grupo_paciente, 'resultado', x.resultado,
                   'estado', CASE WHEN x.ok THEN 'green:' || x.si ELSE 'red:' || x.no END,
                   'validadoPor', usuario_texto(p.validado_por),
                   'fecha', fecha_hora_texto(p.creado_en)
               ) ORDER BY p.creado_en, s.codigo, x.orden), '[]'::jsonb)
          INTO r
          FROM detalle_pruebas_cruzadas p
          JOIN solicitudes_transfusionales s ON s.id = p.solicitud_id
          JOIN hemocomponentes h ON h.id = p.hemocomponente_id
          CROSS JOIN LATERAL (VALUES
              (1, 'Prueba mayor',                 p.resultado_mayor, p.prueba_mayor,         'Compatible', 'Incompatible'),
              (2, 'Prueba menor',                 p.resultado_menor, p.prueba_menor,         'Compatible', 'Incompatible'),
              (3, 'Rastreo de anticuerpos (RAI)', p.resultado_rai,   p.rastreo_anticuerpos,  'Negativo',   'Positivo')
          ) AS x(orden, prueba, resultado, ok, si, no);

    WHEN 'hemovigilancia' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'id', e.id, 'fecha', to_char(e.creado_en AT TIME ZONE 'America/Lima', 'DD/MM/YYYY'),
                   'paciente', e.paciente, 'reaccion', e.tipo_evento, 'din', coalesce(e.din_texto, '—'),
                   'severidad', e.severidad, 'estado', e.estado
               ) ORDER BY e.creado_en DESC), '[]'::jsonb)
          INTO r FROM eventos_hemovigilancia e;

    WHEN 'intercambios' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'id', i.id,
                   'establecimiento', CASE WHEN o.es_sede_local THEN d.nombre ELSE o.nombre END,
                   'componente', tipo_a_producto(i.tipo_hemocomponente),
                   'grupo', grupo_a_texto(i.grupo_abo, i.factor_rh),
                   'unidades', i.cantidad,
                   'producto', tipo_a_producto(i.tipo_hemocomponente) || ' ' || grupo_a_texto(i.grupo_abo, i.factor_rh),
                   'cantidad', i.cantidad || ' un.',
                   'direccion', CASE WHEN o.es_sede_local THEN 'Saliente' ELSE 'Entrante' END,
                   'estado', CASE i.estado WHEN 'pendiente' THEN 'amber:Pendiente'
                                           WHEN 'aceptada'  THEN 'green:Aceptada'
                                           ELSE 'blue:Devuelta' END
               ) ORDER BY i.creado_en DESC), '[]'::jsonb)
          INTO r
          FROM solicitudes_interhospitalarias i
          JOIN establecimientos_red o ON o.id = i.establecimiento_origen_id
          JOIN establecimientos_red d ON d.id = i.establecimiento_destino_id;

    WHEN 'establecimientos' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_object(
                   'nombre', e.nombre, 'red', e.red, 'convenio', e.convenio_descripcion,
                   'desde', fecha_texto(e.convenio_vigente_desde), 'hasta', fecha_texto(e.convenio_vigente_hasta)
               ) ORDER BY e.nombre), '[]'::jsonb)
          INTO r FROM establecimientos_red e WHERE NOT e.es_sede_local;

    WHEN 'stock' THEN
        -- Unidades disponibles por grupo sanguíneo. La capacidad de almacenamiento de cada grupo
        -- es un parámetro (columna capacidad): se puede ajustar aquí. Crítico = menos del 20 % de la capacidad.
        SELECT coalesce(jsonb_agg(jsonb_build_array(g.etiqueta, coalesce(s.n, 0), g.capacidad,
                                                    coalesce(s.n, 0) < g.capacidad * 0.2) ORDER BY g.orden), '[]'::jsonb)
          INTO r
          FROM (VALUES (1, 'O+',  'O',  'positivo', 40), (2, 'O-',  'O',  'negativo', 20),
                       (3, 'A+',  'A',  'positivo', 35), (4, 'A-',  'A',  'negativo', 10),
                       (5, 'B+',  'B',  'positivo', 30), (6, 'B-',  'B',  'negativo', 10),
                       (7, 'AB+', 'AB', 'positivo', 15), (8, 'AB-', 'AB', 'negativo', 10)
               ) AS g(orden, etiqueta, abo, rh, capacidad)
          LEFT JOIN LATERAL (SELECT count(*) AS n
                               FROM hemocomponentes h
                               JOIN unidades_sangre u ON u.id = h.unidad_matriz_id
                              WHERE h.estado = 'disponible'
                                AND u.grupo_abo = g.abo::grupo_abo AND u.factor_rh = g.rh::factor_rh) s ON true;

    WHEN 'alertas' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_array(a.tipo, a.titulo, a.mensaje) ORDER BY a.creado_en DESC), '[]'::jsonb)
          INTO r FROM (SELECT * FROM alertas WHERE NOT atendida ORDER BY creado_en DESC LIMIT 6) a;

    WHEN 'auditoria' THEN
        SELECT coalesce(jsonb_agg(jsonb_build_array(
                   fecha_hora_texto(l.creado_en, true),
                   coalesce(l.detalle->>'usuario', '—'),
                   l.accion, coalesce(l.entidad, '—'), coalesce(l.ip_terminal, '—')
               ) ORDER BY l.creado_en DESC), '[]'::jsonb)
          INTO r FROM (SELECT * FROM auditoria_logs ORDER BY creado_en DESC LIMIT 300) l;

    ELSE
        RAISE EXCEPTION 'Colección desconocida: %', p_coleccion;
    END CASE;
    RETURN r;
END
$$;


-- ---------------------------------------------------------------------
-- ESCRITURA: api_sincronizar(cambios, correo, rol)
--   cambios trae, según la pantalla, las listas completas
--   (usuarios, donantes, inventory, serologia, solicitudes, pruebas,
--   hemovigilancia, intercambios) y las novedades por agregar
--   (auditNuevos, alertasNuevas, despachosNuevos).
--   Solo INSERTA o ACTUALIZA: nada se borra (el historial clínico no se
--   elimina; se cambia de estado).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION api_sincronizar(p jsonb, p_correo text, p_rol text) RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    v_actor  uuid := actor_id(p_correo, p_rol);
    r        record;
    v_root   text;
    v_exp    date;
    v_tipo   tipo_hemocomponente;
    v_cam    uuid;
    v_bolsa  uuid;
    v_don    uuid;
    v_dnt    uuid;
    v_tdon   tipo_donacion;
    v_abo    grupo_abo;
    v_rh     factor_rh;
    v_hc_falta text;
BEGIN
    -- 1. Usuarios ------------------------------------------------------
    IF p ? 'usuarios' THEN
        INSERT INTO usuarios (dni, correo, contrasena_hash, nombres, apellidos, rol, activo)
        SELECT x.dni, lower(x.correo), crypt('Banco2026', gen_salt('bf')),   -- clave temporal de una cuenta nueva
               nombres_de(x.nombre), apellidos_de(x.nombre), rol_a_enum(x.rol), coalesce(x.activo, true)
          FROM jsonb_to_recordset(p->'usuarios') AS x(nombre text, rol text, activo boolean, dni text, correo text)
         WHERE coalesce(x.correo, '') <> '' AND coalesce(x.dni, '') <> ''
        ON CONFLICT (correo) DO UPDATE
           SET rol = EXCLUDED.rol, activo = EXCLUDED.activo,
               nombres = EXCLUDED.nombres, apellidos = EXCLUDED.apellidos, actualizado_en = now();
    END IF;

    -- 2. Donantes ------------------------------------------------------
    IF p ? 'donantes' THEN
        INSERT INTO donantes (dni, nombres, apellidos, grupo_abo, factor_rh, tipo_donacion, total_donaciones,
                              ultima_donacion, apto, diferido, diferimiento_tipo, motivo_diferimiento,
                              diferimiento_hasta, fecha_nacimiento, sexo, peso_kg, hemoglobina_gdl, telefono, correo,
                              direccion, antecedentes_viaje, campana, consentimiento_firmado)
        SELECT x.dni, nombres_de(x.nombre), apellidos_de(x.nombre), abo_de_texto(x.abo), rh_de_texto(x.abo),
               tipo_donacion_de_texto(x.tipo), coalesce(x.donaciones, 0), texto_a_fecha(x."ultimaDonacion"),
               split_part(x.estado, ':', 1) = 'green', split_part(x.estado, ':', 1) <> 'green',
               CASE WHEN split_part(x.estado, ':', 1) = 'green' THEN NULL
                    WHEN split_part(x.estado, ':', 1) = 'red' THEN 'Permanente' ELSE 'Temporal' END,
               CASE WHEN split_part(x.estado, ':', 1) <> 'green' THEN coalesce(nullif(x."motivoDiferimiento", ''), 'Sin motivo registrado') END,
               CASE WHEN split_part(x.estado, ':', 1) = 'amber' THEN texto_a_fecha(x."vigenciaDiferimiento") END,
               texto_a_fecha(x."fechaNacimiento"), x.sexo, x."pesoKg", x.hemoglobina, x.telefono, x.correo,
               x.direccion, x.antecedentes,
               coalesce(nullif(x.campana, ''), 'Donación directa'), coalesce(x.consentimiento, false)
          FROM jsonb_to_recordset(p->'donantes')
               AS x(nombre text, dni text, abo text, tipo text, "ultimaDonacion" text, donaciones int, estado text,
                    "motivoDiferimiento" text, "vigenciaDiferimiento" text, "fechaNacimiento" text,
                    "pesoKg" numeric, antecedentes text, consentimiento boolean, sexo text, telefono text,
                    correo text, direccion text, hemoglobina numeric, campana text)
         WHERE coalesce(x.dni, '') <> ''
        ON CONFLICT (dni) DO UPDATE
           SET grupo_abo = EXCLUDED.grupo_abo, factor_rh = EXCLUDED.factor_rh, tipo_donacion = EXCLUDED.tipo_donacion,
               total_donaciones = EXCLUDED.total_donaciones, ultima_donacion = EXCLUDED.ultima_donacion,
               apto = EXCLUDED.apto, diferido = EXCLUDED.diferido, diferimiento_tipo = EXCLUDED.diferimiento_tipo,
               motivo_diferimiento = EXCLUDED.motivo_diferimiento, diferimiento_hasta = EXCLUDED.diferimiento_hasta,
               fecha_nacimiento = EXCLUDED.fecha_nacimiento, sexo = EXCLUDED.sexo, peso_kg = EXCLUDED.peso_kg,
               hemoglobina_gdl = EXCLUDED.hemoglobina_gdl, telefono = EXCLUDED.telefono, correo = EXCLUDED.correo,
               direccion = EXCLUDED.direccion, antecedentes_viaje = EXCLUDED.antecedentes_viaje,
               campana = EXCLUDED.campana,
               consentimiento_firmado = donantes.consentimiento_firmado OR EXCLUDED.consentimiento_firmado;
    END IF;

    -- 3. Inventario (bolsas y hemocomponentes) -------------------------
    IF p ? 'inventory' THEN
        FOR r IN
            SELECT * FROM jsonb_to_recordset(p->'inventory')
                   AS x(din text, producto text, abo text, camara text, exp text, status text, donante text)
             ORDER BY (position('-' in x.din) > 0), x.din          -- primero las bolsas, luego sus componentes
        LOOP
            v_root := split_part(r.din, '-', 1);
            v_exp  := texto_a_fecha(r.exp);
            v_tipo := producto_a_tipo(r.producto);
            IF v_tipo IS NULL THEN RAISE EXCEPTION 'Hemocomponente desconocido: %', r.producto; END IF;
            IF v_exp IS NULL THEN RAISE EXCEPTION 'Fecha de vencimiento inválida en la unidad %', r.din; END IF;
            SELECT id INTO v_cam FROM camaras_refrigeracion WHERE etiqueta = r.camara;

            SELECT id INTO v_bolsa FROM unidades_sangre WHERE din = v_root;
            IF v_bolsa IS NULL THEN
                v_abo := abo_de_texto(r.abo);
                v_rh  := rh_de_texto(r.abo);
                IF v_abo IS NULL OR v_rh IS NULL THEN RAISE EXCEPTION 'Grupo sanguíneo inválido en la unidad %', r.din; END IF;
                v_dnt := NULL;
                IF coalesce(r.donante, '') NOT IN ('', '—') THEN
                    SELECT id INTO v_dnt FROM donantes WHERE dni = r.donante;
                END IF;
                INSERT INTO unidades_sangre (donante_id, responsable_id, din, grupo_abo, factor_rh, fecha_vencimiento, estado)
                VALUES (v_dnt, CASE WHEN v_dnt IS NOT NULL THEN v_actor END, v_root, v_abo, v_rh, v_exp, r.status::estado_unidad)
                RETURNING id INTO v_bolsa;
            ELSIF position('-' in r.din) = 0 THEN
                UPDATE unidades_sangre SET estado = r.status::estado_unidad, fecha_vencimiento = v_exp WHERE id = v_bolsa;
            END IF;

            INSERT INTO hemocomponentes (unidad_matriz_id, codigo_producto, din_derivado, tipo, fecha_vencimiento, estado, camara_id)
            VALUES (v_bolsa, tipo_a_codigo(v_tipo), r.din, v_tipo, v_exp, r.status::estado_unidad, v_cam)
            ON CONFLICT (din_derivado) DO UPDATE
               SET estado = EXCLUDED.estado, camara_id = EXCLUDED.camara_id, fecha_vencimiento = EXCLUDED.fecha_vencimiento,
                   descartado_por = CASE WHEN EXCLUDED.estado = 'incinerado' AND hemocomponentes.estado <> 'incinerado'
                                         THEN v_actor ELSE hemocomponentes.descartado_por END,
                   descartado_en  = CASE WHEN EXCLUDED.estado = 'incinerado' AND hemocomponentes.estado <> 'incinerado'
                                         THEN now() ELSE hemocomponentes.descartado_en END;
        END LOOP;
    END IF;

    -- 4. Tamizaje serológico (una fila por bolsa) ----------------------
    IF p ? 'serologia' THEN
        IF EXISTS (SELECT 1 FROM jsonb_to_recordset(p->'serologia') AS s(dig1 text, dig2 text) WHERE s.dig1 IS DISTINCT FROM s.dig2) THEN
            RAISE EXCEPTION 'Las dos digitaciones del tamizaje no coinciden: no se puede guardar';
        END IF;
        INSERT INTO detalle_tamizaje (unidad_matriz_id, vih, hepatitis_b, hepatitis_c, sifilis, chagas, htlv, doble_digitacion, registrado_por)
        SELECT u.id,
               resultado_de_texto(max(CASE WHEN s.marcador = 'VIH 1/2'             THEN s.resultado END)),
               resultado_de_texto(max(CASE WHEN s.marcador = 'Hepatitis B (HBsAg)' THEN s.resultado END)),
               resultado_de_texto(max(CASE WHEN s.marcador = 'Hepatitis C'         THEN s.resultado END)),
               resultado_de_texto(max(CASE WHEN s.marcador = 'Sífilis (VDRL/RPR)'  THEN s.resultado END)),
               resultado_de_texto(max(CASE WHEN s.marcador = 'Chagas'              THEN s.resultado END)),
               resultado_de_texto(max(CASE WHEN s.marcador = 'HTLV I/II'           THEN s.resultado END)),
               true, v_actor
          FROM jsonb_to_recordset(p->'serologia') AS s(din text, marcador text, resultado text)
          JOIN unidades_sangre u ON u.din = s.din
         GROUP BY u.id
        HAVING count(DISTINCT s.marcador) FILTER (WHERE s.marcador IN
               ('VIH 1/2','Hepatitis B (HBsAg)','Hepatitis C','Sífilis (VDRL/RPR)','Chagas','HTLV I/II')) = 6
        ON CONFLICT (unidad_matriz_id) DO UPDATE
           SET vih = EXCLUDED.vih, hepatitis_b = EXCLUDED.hepatitis_b, hepatitis_c = EXCLUDED.hepatitis_c,
               sifilis = EXCLUDED.sifilis, chagas = EXCLUDED.chagas, htlv = EXCLUDED.htlv,
               registrado_por = v_actor, creado_en = now()
         WHERE (detalle_tamizaje.vih, detalle_tamizaje.hepatitis_b, detalle_tamizaje.hepatitis_c,
                detalle_tamizaje.sifilis, detalle_tamizaje.chagas, detalle_tamizaje.htlv)
               IS DISTINCT FROM (EXCLUDED.vih, EXCLUDED.hepatitis_b, EXCLUDED.hepatitis_c,
                                 EXCLUDED.sifilis, EXCLUDED.chagas, EXCLUDED.htlv);
    END IF;

    -- 5. Solicitudes de sangre -----------------------------------------
    IF p ? 'pacientes' THEN
        INSERT INTO pacientes (dni, historia_clinica, nombres, fecha_nacimiento, sexo, grupo_abo, factor_rh,
                               peso_kg, hemoglobina_gdl, telefono, correo, direccion)
        SELECT x.dni, x.hc, x.paciente, texto_a_fecha(x."fechaNacimiento"), x.sexo, abo_de_texto(x.abo), rh_de_texto(x.abo),
               x."pesoKg", x.hemoglobina, x.telefono, x.correo, x.direccion
          FROM jsonb_to_recordset(p->'pacientes')
               AS x(dni text, hc text, paciente text, "fechaNacimiento" text, sexo text, abo text, "pesoKg" numeric,
                    hemoglobina numeric, telefono text, correo text, direccion text)
         WHERE coalesce(x.hc, '') <> ''
        ON CONFLICT (historia_clinica) DO UPDATE
           SET dni = EXCLUDED.dni, nombres = EXCLUDED.nombres, fecha_nacimiento = EXCLUDED.fecha_nacimiento,
               sexo = EXCLUDED.sexo, grupo_abo = EXCLUDED.grupo_abo, factor_rh = EXCLUDED.factor_rh,
               peso_kg = EXCLUDED.peso_kg, hemoglobina_gdl = EXCLUDED.hemoglobina_gdl, telefono = EXCLUDED.telefono,
               correo = EXCLUDED.correo, direccion = EXCLUDED.direccion;
    END IF;

    IF p ? 'solicitudes' THEN
        -- El paciente debe estar registrado antes (módulo Pacientes): la solicitud no crea pacientes a medias
        SELECT x.hc INTO v_hc_falta
          FROM jsonb_to_recordset(p->'solicitudes') AS x(id text, hc text)
          LEFT JOIN pacientes pa ON pa.historia_clinica = x.hc
         WHERE coalesce(x.id, '') <> '' AND pa.id IS NULL
         LIMIT 1;
        IF v_hc_falta IS NOT NULL THEN
            RAISE EXCEPTION 'La historia clínica % no está registrada: registra primero al paciente en el módulo Pacientes', v_hc_falta;
        END IF;

        INSERT INTO solicitudes_transfusionales (codigo, medico_id, servicio, paciente_id,
               diagnostico_cie10, diagnostico_texto, componente, prioridad, prioridad_texto, tipo_solicitud,
               fecha_liberacion_reserva, reserva_texto, estado)
        SELECT x.id, v_actor, coalesce(x.servicio, '—'), pa.id,
               left(coalesce(nullif(split_part(x.cie10, ' — ', 1), ''), '—'), 10), coalesce(nullif(x.cie10, ''), '—'),
               coalesce(nullif(x.componente, ''), '1 unidad'),
               (CASE WHEN x.prioridad LIKE 'Urgente%' THEN 'urgente' ELSE 'rutina' END)::prioridad_clinica,
               coalesce(nullif(x.prioridad, ''), 'Programada'), coalesce(x.tipo, 'transfusional'), texto_a_fecha(x."reservaFecha"), x."reservaFecha",
               coalesce(x.estado, 'pendiente')
          FROM jsonb_to_recordset(p->'solicitudes')
               AS x(id text, paciente text, dni text, hc text, servicio text, cie10 text, componente text, prioridad text,
                    tipo text, "reservaFecha" text, estado text)
          JOIN pacientes pa ON pa.historia_clinica = x.hc
         WHERE coalesce(x.id, '') <> ''
        ON CONFLICT (codigo) DO UPDATE SET estado = EXCLUDED.estado;
    END IF;

    -- 6. Pruebas de compatibilidad (última tanda por solicitud) --------
    IF p ? 'pruebas' THEN
        WITH g AS (
            SELECT x."solicitudId" AS sid, max(x.muestra) AS muestra, max(x.unidad) AS unidad, max(x."grupoPaciente") AS grupo,
                   max(CASE WHEN x.prueba = 'Prueba mayor'                 THEN x.resultado END) AS rmayor,
                   max(CASE WHEN x.prueba = 'Prueba menor'                 THEN x.resultado END) AS rmenor,
                   max(CASE WHEN x.prueba = 'Rastreo de anticuerpos (RAI)' THEN x.resultado END) AS rrai
              FROM jsonb_to_recordset(p->'pruebas')
                   AS x("solicitudId" text, prueba text, muestra text, unidad text, "grupoPaciente" text, resultado text)
             GROUP BY x."solicitudId")
        INSERT INTO detalle_pruebas_cruzadas (solicitud_id, hemocomponente_id, prueba_mayor, prueba_menor, rastreo_anticuerpos,
               compatible, validado_por, muestra, grupo_paciente, resultado_mayor, resultado_menor, resultado_rai)
        SELECT s.id, h.id, g.rmayor = 'Sin aglutinación', g.rmenor = 'Sin aglutinación', g.rrai = 'Negativo',
               (g.rmayor = 'Sin aglutinación' AND g.rmenor = 'Sin aglutinación' AND g.rrai = 'Negativo'),
               v_actor, coalesce(g.muestra, '—'), coalesce(g.grupo, '—'), g.rmayor, g.rmenor, g.rrai
          FROM g
          JOIN solicitudes_transfusionales s ON s.codigo = g.sid
          JOIN hemocomponentes h ON h.din_derivado = g.unidad
         WHERE g.rmayor IS NOT NULL AND g.rmenor IS NOT NULL AND g.rrai IS NOT NULL
        ON CONFLICT (solicitud_id) DO UPDATE
           SET hemocomponente_id = EXCLUDED.hemocomponente_id, prueba_mayor = EXCLUDED.prueba_mayor,
               prueba_menor = EXCLUDED.prueba_menor, rastreo_anticuerpos = EXCLUDED.rastreo_anticuerpos,
               compatible = EXCLUDED.compatible, muestra = EXCLUDED.muestra, grupo_paciente = EXCLUDED.grupo_paciente,
               resultado_mayor = EXCLUDED.resultado_mayor, resultado_menor = EXCLUDED.resultado_menor,
               resultado_rai = EXCLUDED.resultado_rai, validado_por = v_actor, creado_en = now()
         WHERE (detalle_pruebas_cruzadas.hemocomponente_id, detalle_pruebas_cruzadas.resultado_mayor, detalle_pruebas_cruzadas.resultado_menor,
                detalle_pruebas_cruzadas.resultado_rai, detalle_pruebas_cruzadas.muestra)
               IS DISTINCT FROM (EXCLUDED.hemocomponente_id, EXCLUDED.resultado_mayor, EXCLUDED.resultado_menor,
                                 EXCLUDED.resultado_rai, EXCLUDED.muestra);
    END IF;

    -- 7. Hemovigilancia -------------------------------------------------
    IF p ? 'hemovigilancia' THEN
        INSERT INTO eventos_hemovigilancia (id, hemocomponente_id, donante_id, tipo_evento, reportado_por,
                                            paciente, din_texto, severidad, estado)
        SELECT x.id::uuid, h.id, dn.id, x.reaccion, v_actor, coalesce(nullif(x.paciente, ''), '—'), coalesce(nullif(x.din, ''), '—'), x.severidad, x.estado
          FROM jsonb_to_recordset(p->'hemovigilancia')
               AS x(id text, paciente text, reaccion text, din text, severidad text, estado text)
          LEFT JOIN hemocomponentes h ON h.din_derivado = x.din
          LEFT JOIN unidades_sangre u ON u.id = h.unidad_matriz_id
          LEFT JOIN donantes dn ON dn.id = u.donante_id
         WHERE coalesce(x.id, '') <> ''
        ON CONFLICT (id) DO UPDATE SET severidad = EXCLUDED.severidad, estado = EXCLUDED.estado;
    END IF;

    -- 8. Red interhospitalaria -----------------------------------------
    IF p ? 'intercambios' THEN
        INSERT INTO solicitudes_interhospitalarias (id, establecimiento_origen_id, establecimiento_destino_id, grupo_abo,
               factor_rh, tipo_hemocomponente, cantidad, estado, es_devolucion, respondido_en, motivo)
        SELECT x.id::uuid,
               CASE WHEN x.direccion = 'Saliente' THEN sede.id ELSE otro.id END,
               CASE WHEN x.direccion = 'Saliente' THEN otro.id ELSE sede.id END,
               abo_de_texto(x.grupo), rh_de_texto(x.grupo), producto_a_tipo(x.componente), x.unidades,
               CASE split_part(x.estado, ':', 1) WHEN 'amber' THEN 'pendiente' WHEN 'green' THEN 'aceptada' ELSE 'devuelta' END,
               split_part(x.estado, ':', 1) NOT IN ('amber', 'green'),
               CASE WHEN split_part(x.estado, ':', 1) <> 'amber' THEN now() END,
               x.motivo
          FROM jsonb_to_recordset(p->'intercambios')
               AS x(id text, establecimiento text, componente text, grupo text, unidades int, direccion text, estado text, motivo text)
          JOIN establecimientos_red otro ON otro.nombre = x.establecimiento
         CROSS JOIN (SELECT id FROM establecimientos_red WHERE es_sede_local LIMIT 1) sede
         WHERE coalesce(x.id, '') <> ''
        ON CONFLICT (id) DO UPDATE
           SET estado = EXCLUDED.estado, es_devolucion = EXCLUDED.es_devolucion,
               respondido_en = coalesce(solicitudes_interhospitalarias.respondido_en, EXCLUDED.respondido_en);
    END IF;

    -- 9. Novedades que solo se agregan ----------------------------------
    IF p ? 'auditNuevos' THEN
        INSERT INTO auditoria_logs (usuario_id, accion, entidad, ip_terminal, detalle, creado_en)
        SELECT v_actor, x.accion, x.recurso, x.ip, jsonb_build_object('usuario', x.usuario),
               clock_timestamp() + (x.n * interval '1 microsecond')
          FROM ROWS FROM (jsonb_to_recordset(p->'auditNuevos') AS (usuario text, accion text, recurso text, ip text))
               WITH ORDINALITY AS x(usuario, accion, recurso, ip, n);
    END IF;

    IF p ? 'alertasNuevas' THEN
        INSERT INTO alertas (tipo, titulo, mensaje, creado_en)
        SELECT coalesce(x.color, 'blue'), x.titulo, coalesce(x.detalle, ''), clock_timestamp() + (x.n * interval '1 microsecond')
          FROM ROWS FROM (jsonb_to_recordset(p->'alertasNuevas') AS (color text, titulo text, detalle text))
               WITH ORDINALITY AS x(color, titulo, detalle, n);
    END IF;

    IF p ? 'despachosNuevos' THEN
        INSERT INTO detalle_despachos (solicitud_id, hemocomponente_id, escaneo_hemocomponente, escaneo_pulsera_receptor,
                               autorizado_por, historia_clinica)
        SELECT (SELECT s.id FROM solicitudes_transfusionales s JOIN pacientes pa ON pa.id = s.paciente_id
                 WHERE pa.historia_clinica = x.hc ORDER BY s.creado_en DESC LIMIT 1),
               h.id, true, true, v_actor, x.hc
          FROM jsonb_to_recordset(p->'despachosNuevos') AS x(din text, hc text)
          JOIN hemocomponentes h ON h.din_derivado = x.din;
    END IF;
END
$$;

-- Inicio de sesión: devuelve {nombre, correo, rol} si el usuario (correo o DNI) existe, está activo y la
-- contraseña coincide con el hash bcrypt guardado; si no, devuelve NULL.
CREATE OR REPLACE FUNCTION api_autenticar(p_usuario text, p_clave text) RETURNS jsonb LANGUAGE sql STABLE AS $$
    SELECT jsonb_build_object(
               'nombre', CASE WHEN u.apellidos = '' THEN u.nombres ELSE u.nombres || ' ' || u.apellidos END,
               'correo', u.correo,
               'rol',    rol_a_texto(u.rol))
      FROM usuarios u
     WHERE (lower(u.correo) = lower(coalesce(p_usuario, '')) OR u.dni = coalesce(p_usuario, ''))
       AND u.activo
       AND u.contrasena_hash = crypt(coalesce(p_clave, ''), u.contrasena_hash)
     LIMIT 1;
$$;

-- Último acceso de la cuenta que inicia sesión (si existe en la base)
CREATE OR REPLACE FUNCTION api_registrar_acceso(p_usuario text) RETURNS void LANGUAGE sql AS $$
    UPDATE usuarios SET ultimo_acceso = now()
     WHERE lower(correo) = lower(coalesce(p_usuario, '')) OR dni = coalesce(p_usuario, '');
$$;

-- Vacía todas las tablas (solo para "Restablecer datos de ejemplo")
CREATE OR REPLACE FUNCTION api_vaciar() RETURNS void LANGUAGE sql AS $$
    TRUNCATE detalle_despachos, detalle_pruebas_cruzadas, solicitudes_transfusionales, pacientes, eventos_hemovigilancia, detalle_tamizaje,
             hemocomponentes, unidades_sangre, donantes, camaras_refrigeracion,
             solicitudes_interhospitalarias, establecimientos_red, alertas, auditoria_logs, usuarios CASCADE;
$$;

-- ---------------------------------------------------------------------
-- Próxima cita del donante: se calcula sola para que la columna nunca quede vacía
--   diferido temporal -> el día en que termina el diferimiento
--   diferido permanente -> sin cita (queda nulo a propósito)
--   apto con donación previa -> última donación + 90 días
--   apto sin donaciones -> desde su registro
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION donantes_proxima_cita() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.diferido THEN
        NEW.proxima_cita_en := CASE WHEN NEW.diferimiento_tipo = 'Temporal' THEN NEW.diferimiento_hasta::timestamptz END;
    ELSIF NEW.ultima_donacion IS NOT NULL THEN
        NEW.proxima_cita_en := (NEW.ultima_donacion + 90)::timestamptz;
    ELSE
        NEW.proxima_cita_en := coalesce(NEW.proxima_cita_en, now());
    END IF;
    RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS trg_donantes_proxima_cita ON donantes;
CREATE TRIGGER trg_donantes_proxima_cita BEFORE INSERT OR UPDATE ON donantes
    FOR EACH ROW EXECUTE FUNCTION donantes_proxima_cita();

-- Donantes ya guardados: se rellena la columna una sola vez
UPDATE donantes SET proxima_cita_en = NULL WHERE proxima_cita_en IS NULL;

-- ################ datos.sql ################
-- =====================================================================
--  Datos de ejemplo iniciales (HLEV - Banco de Sangre Tipo II)
--  Se cargan SOLO si la base está vacía (no hay usuarios), así que es
--  seguro ejecutarlo en cada arranque.
--  Cuentas del equipo del proyecto (7 integrantes). Solo se guarda el HASH bcrypt de cada
--  contraseña, nunca la contraseña en claro. Los DNI de las cuentas son ficticios.
-- =====================================================================
DO $$
DECLARE
    v_tec   uuid;
    v_med   uuid;
    v_hlev  uuid;
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios) THEN
        RETURN;
    END IF;

    -- Usuarios (el más reciente primero, como se listan en Administración) ---
    INSERT INTO usuarios (dni, correo, contrasena_hash, nombres, apellidos, rol, activo, ultimo_acceso, creado_en) VALUES
      ('70000001', 'jcangana@hlev.gob.pe', '$2a$10$AKGEzh98UOY.mlWQEGAeIufDv/dljnCgebtMOXLCRrYeqqOIPmR0a', 'José Leonel', 'Cangana Salcedo', 'administrador', true, '2026-09-29 09:12-05', '2026-09-01 10:07-05'),
      ('70000002', 'jpalomino@hlev.gob.pe', '$2a$10$2//2nKOC7Wv3fM.Re4J3UuGEEJLsIl1cVIZA.4bKc.FuegpHXjiPC', 'José William', 'Palomino Camarena', 'tecnologo_medico', true, '2026-09-29 08:55-05', '2026-09-01 10:06-05'),
      ('70000003', 'jjaimes@hlev.gob.pe', '$2a$10$8/WsFyOrSDd1sPwbXLMytOW6V1LE9hoCikRugBQ5GdBP01HefsoZy', 'Julibeth Antonela', 'Jaimes Daza', 'jefe_banco_sangre', true, '2026-09-28 17:40-05', '2026-09-01 10:05-05'),
      ('70000004', 'dbatallanos@hlev.gob.pe', '$2a$10$p3zcvDp9Fzue3Zns8DVDJes4D.LeZF3BOqHz36F8S9PxBoWnV7GtC', 'Daniel Enrique', 'Batallanos Ibarra', 'medico_solicitante', true, '2026-09-12 11:20-05', '2026-09-01 10:04-05'),
      ('70000005', 'acamones@hlev.gob.pe', '$2a$10$.OKaCWzzyThFvZh5Vzz3t.0bs1FT6N8QORFL71Vy2lE8M1c8gMhma', 'Antony David', 'Camones Chávez', 'tecnologo_medico', true, '2026-09-29 10:20-05', '2026-09-01 10:03-05'),
      ('70000006', 'jyangali@hlev.gob.pe', '$2a$10$dlWOsAHoDbi.k/4y0n9f4Ob3FpVWZ2rn8sIofeyCK6t1UgpacyeiS', 'Jesús Alberto', 'Yangali Saravia', 'administrador', true, '2026-09-29 07:30-05', '2026-09-01 10:02-05'),
      ('70000007', 'aaylas@hlev.gob.pe', '$2a$10$tY5pa2R1mLclh92zSwux9.FmEHO4LhwgXDVOucYTkA0rcazwFO/Te', 'Alexander Hernan', 'Aylas Valdez', 'medico_solicitante', true, '2026-09-29 07:05-05', '2026-09-01 10:01-05');
    SELECT id INTO v_tec FROM usuarios WHERE correo = 'jpalomino@hlev.gob.pe';
    SELECT id INTO v_med FROM usuarios WHERE correo = 'dbatallanos@hlev.gob.pe';

    -- Establecimientos de la red -------------------------------------------
    INSERT INTO establecimientos_red (nombre, red, convenio_descripcion, convenio_vigente_desde, convenio_vigente_hasta, es_sede_local) VALUES
      ('HLEV (sede local)',              'MINSA - Lima Este', NULL, NULL, NULL, true),
      ('Hospital Vitarte II',            'MINSA - Lima Este', 'Intercambio recíproco de hemocomponentes',            '2026-01-01', '2026-12-31', false),
      ('Hospital de la Solidaridad ATE', 'MINSA - Lima Este', 'Apoyo mutuo ante desabastecimiento crítico',          '2026-06-16', '2027-06-15', false),
      ('PRONAHEBAS Central',             'Red nacional',      'Convenio marco de red nacional de bancos de sangre',  NULL, NULL, false);
    SELECT id INTO v_hlev FROM establecimientos_red WHERE es_sede_local;

    -- Cámaras ---------------------------------------------------------------
    INSERT INTO camaras_refrigeracion (nombre, etiqueta, tipo, temp_min, temp_max, ultima_temperatura, ultima_lectura_en, fuera_de_rango, ultima_calibracion) VALUES
      ('Cámara 1 · Refrigeración', 'Cámara 1 · Refrig.',  'refrigeracion', 2, 6,      4.1,  '2026-09-29 09:00-05', false, '2026-08-15'),
      ('Cámara 2 · Refrigeración', 'Cámara 2 · Refrig.',  'refrigeracion', 2, 6,      6.8,  '2026-09-29 09:00-05', true,  '2026-08-15'),
      ('Congelador 1',             'Congelador 1',        'congelacion',   -30, -18,  -24.3, '2026-09-29 09:00-05', false, '2026-07-20'),
      ('Cámara 3 · Agitador',      'Cámara 3 · Agitador', 'agitador',      20, 24,     NULL, NULL, false, '2026-08-01'),
      ('Congelador 2',             'Congelador 2',        'congelacion',   -30, -18,  NULL, NULL, false, '2026-07-20');

    -- Donantes --------------------------------------------------------------
    INSERT INTO donantes (dni, nombres, apellidos, fecha_nacimiento, sexo, grupo_abo, factor_rh, peso_kg, hemoglobina_gdl,
                          telefono, correo, direccion, antecedentes_viaje, campana, tipo_donacion, total_donaciones,
                          ultima_donacion, apto, diferido, diferimiento_tipo, motivo_diferimiento, diferimiento_hasta,
                          consentimiento_firmado, creado_en) VALUES
      ('45678912', 'María Fernanda', 'Quispe Rojas',   '1994-03-12', 'F', 'O',  'negativo', 62.0, 13.4, '987654321', 'maria.quispe@gmail.com',   'Av. Nicolás Ayllón 1520, Ate',          'Ninguno', 'Dona Vida UTP 2026-II',   'voluntaria', 7,  '2026-09-12', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:07-05'),
      ('41235678', 'Carlos Alberto', 'Ramírez Soto',   '1988-07-25', 'M', 'A',  'positivo', 78.5, 14.8, '986543210', 'carlos.ramirez@gmail.com', 'Jr. Los Pinos 245, Vitarte',          'Viaje a zona endémica de malaria', 'Donación directa', 'reposicion', 3,  '2026-08-02', false, true,  'Temporal',   'Viaje a zona endémica de malaria', '2026-12-12', true, '2026-09-01 09:06-05'),
      ('72345891', 'Ana Lucía',      'Fernández Luna', '1999-11-03', 'F', 'O',  'positivo', 55.0, 11.2, '985432109', 'ana.fernandez@gmail.com',  'Calle Las Flores 88, Santa Anita',      'Ninguno', 'Empresas Solidarias ATE', 'voluntaria', 12, '2026-08-28', false, true,  'Temporal',   'Hemoglobina bajo el mínimo (11.2 g/dL)', '2026-10-30', true, '2026-09-01 09:05-05'),
      ('09876543', 'Luis Miguel',    'Huamán Castro',  '1985-01-18', 'M', 'B',  'positivo', 84.0, 15.1, '984321098', 'luis.huaman@gmail.com',    'Av. Separadora Industrial 730, Ate',    'Ninguno', 'Donación directa',        'voluntaria', 5,  '2026-06-05', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:04-05'),
      ('43219876', 'Jorge Antonio',  'Vega Mamani',    '1991-09-09', 'M', 'AB', 'positivo', 72.0, 14.2, '983210987', 'jorge.vega@gmail.com',     'Psje. San Martín 12, Chaclacayo',       'Antecedente de conducta de riesgo declarada', 'Donación directa', 'reposicion', 1,  '2026-06-17', false, true,  'Permanente', 'Antecedente de conducta de riesgo declarada', NULL, true, '2026-09-01 09:03-05'),
      ('70123456', 'Rosa Elena',     'Torres Aliaga',  '1996-05-30', 'F', 'A',  'negativo', 58.5, 13.0, '982109876', 'rosa.torres@gmail.com',    'Av. Carretera Central Km 9, Ate',       'Ninguno', 'Dona Vida UTP 2026-II',   'voluntaria', 9,  '2026-06-10', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:02-05'),
      ('46801357', 'Pedro Luis',     'Chávez Ibarra',  '1990-12-21', 'M', 'O',  'negativo', 80.0, 15.6, '981098765', 'pedro.chavez@gmail.com',   'Jr. Huáscar 410, Vitarte',              'Ninguno', 'Donación directa',        'autologa',   2,  '2026-09-01', true,  false, NULL,         NULL, NULL, true, '2026-09-01 09:01-05');

    -- Unidades del inventario: (DIN, producto, grupo, cámara, vencimiento, estado, DNI donante, extracción)
    CREATE TEMP TABLE _u (din text, tipo tipo_hemocomponente, abo grupo_abo, rh factor_rh, camara text, vence date,
                          estado estado_unidad, dni text, extraccion timestamptz) ON COMMIT DROP;
    INSERT INTO _u VALUES
      ('W1237250171238', 'concentrado_hematies',    'O',  'negativo', 'Cámara 2 · Refrig.',  '2026-10-11', 'disponible', '45678912', '2026-09-06 09:00-05'),
      ('W1237250171012', 'concentrado_hematies',    'O',  'positivo', 'Cámara 1 · Refrig.',  '2026-09-28', 'reservado',  '72345891', '2026-08-24 09:00-05'),
      ('W1237250169887', 'plasma_fresco_congelado', 'A',  'positivo', 'Congelador 1',        '2027-09-30', 'disponible', '41235678', '2026-08-02 09:00-05'),
      ('W1237250165530', 'concentrado_plaquetas',   'B',  'negativo', 'Cámara 3 · Agitador', '2026-09-16', 'cuarentena', NULL,       '2026-09-11 09:00-05'),
      ('W1237250169901', 'concentrado_hematies',    'AB', 'negativo', 'Cámara 2 · Refrig.',  '2026-09-15', 'bloqueado',  NULL,       '2026-08-11 09:00-05'),
      ('W1237250158820', 'crioprecipitado',         'O',  'positivo', 'Congelador 2',        '2027-04-30', 'disponible', NULL,       '2026-04-30 09:00-05'),
      ('W1237250149310', 'concentrado_hematies',    'A',  'negativo', 'Cámara 1 · Refrig.',  '2026-09-01', 'vencido',    '70123456', '2026-07-28 09:00-05'),
      ('W1237250171400', 'concentrado_hematies',    'B',  'positivo', 'Cámara 2 · Refrig.',  '2026-09-20', 'despachado', '09876543', '2026-08-16 09:00-05'),
      ('W1237260000101', 'sangre_total',            'O',  'negativo', 'Cámara 1 · Refrig.',  '2026-10-06', 'cuarentena', '46801357', '2026-09-01 09:00-05');

    -- Bolsa de sangre con su donante de origen (solo las que tienen donante)
    INSERT INTO unidades_sangre (donante_id, responsable_id, din, grupo_abo, factor_rh, fecha_extraccion, fecha_vencimiento, estado)
    SELECT dn.id, CASE WHEN dn.id IS NOT NULL THEN v_tec END, u.din, u.abo, u.rh, u.extraccion, u.vence, u.estado
      FROM _u u LEFT JOIN donantes dn ON dn.dni = u.dni;

    INSERT INTO hemocomponentes (unidad_matriz_id, codigo_producto, din_derivado, tipo, fecha_vencimiento, estado, camara_id)
    SELECT b.id, tipo_a_codigo(u.tipo), u.din, u.tipo, u.vence, u.estado, c.id
      FROM _u u JOIN unidades_sangre b ON b.din = u.din
      LEFT JOIN camaras_refrigeracion c ON c.etiqueta = u.camara;

    -- Solicitudes de sangre y pruebas de compatibilidad ------------------------
    INSERT INTO pacientes (dni, historia_clinica, nombres, fecha_nacimiento, sexo, grupo_abo, factor_rh,
                           peso_kg, hemoglobina_gdl, telefono, correo, direccion) VALUES
      ('48215637', 'HC-2026-00381', 'Marcos Effio Bravo',     '1979-04-16', 'M', 'A', 'positivo', 74.0, 8.9, '976123450', 'marcos.effio@gmail.com',     'Av. Nicolás Ayllón 2210, Ate'),
      ('41987352', 'HC-2026-00355', 'Rosa Quinteros Farfán',  '1990-08-02', 'F', 'O', 'positivo', 66.5, 9.4, '975234561', 'rosa.quinteros@gmail.com',   'Jr. Huáscar 118, Vitarte'),
      ('73456218', 'HC-2026-00312', 'Denis Ibarra León',      '1984-11-27', 'M', 'B', 'positivo', 81.0, 7.8, '974345672', 'denis.ibarra@gmail.com',     'Calle Los Cedros 45, Santa Anita');

    INSERT INTO solicitudes_transfusionales (codigo, medico_id, servicio, paciente_id, diagnostico_cie10,
           diagnostico_texto, componente, prioridad, prioridad_texto, tipo_solicitud, fecha_liberacion_reserva, reserva_texto, estado, creado_en) VALUES
      ('SOL-2026-0341', v_med, 'Emergencia',        (SELECT id FROM pacientes WHERE historia_clinica = 'HC-2026-00381'), 'K92.2', 'K92.2 — Hemorragia digestiva alta',
       '2 unidades — Concentrado de Hematíes', 'urgente', 'Urgente (<15 min)', 'transfusional', NULL, NULL, 'pendiente',  '2026-09-29 08:40-05'),
      ('SOL-2026-0338', v_med, 'Centro Quirúrgico', (SELECT id FROM pacientes WHERE historia_clinica = 'HC-2026-00355'), 'O72.1', 'O72.1 — Hemorragia posparto',
       '3 unidades — Concentrado de Hematíes', 'rutina',  'Programada',        'reserva',       '2026-09-30', '30/09/2026', 'compatible', '2026-09-28 15:10-05'),
      ('SOL-2026-0333', v_med, 'UCI',               (SELECT id FROM pacientes WHERE historia_clinica = 'HC-2026-00312'), 'D62',   'D62 — Hemorragia aguda, anemia',
       '1 unidad — Plasma Fresco Congelado',   'rutina',  'Diferible',         'transfusional', NULL, NULL, 'pendiente',  '2026-09-27 19:25-05');

    INSERT INTO detalle_pruebas_cruzadas (solicitud_id, hemocomponente_id, prueba_mayor, prueba_menor, rastreo_anticuerpos, compatible,
           validado_por, creado_en, muestra, grupo_paciente, resultado_mayor, resultado_menor, resultado_rai)
    SELECT s.id, h.id, true, true, true, true, v_tec, '2026-09-28 16:30-05', 'S-88213', 'O POS', 'Sin aglutinación', 'Sin aglutinación', 'Negativo'
      FROM solicitudes_transfusionales s, hemocomponentes h
     WHERE s.codigo = 'SOL-2026-0338' AND h.din_derivado = 'W1237250171012';

    -- Hemovigilancia ---------------------------------------------------------
    INSERT INTO eventos_hemovigilancia (hemocomponente_id, donante_id, tipo_evento, reportado_por, creado_en, paciente, din_texto, severidad, estado)
    SELECT h.id, b.donante_id, e.reaccion, v_tec, e.cuando::timestamptz, e.paciente, e.din, e.sev, e.estado
      FROM (VALUES
        ('2026-09-10 10:00-05', 'Torres Huamán, L.', 'Reacción febril no hemolítica',  'W1237250171012', 'amber:Leve',  'green:Resuelto'),
        ('2026-09-03 10:00-05', 'Quiroz Vera, D.',   'Reacción alérgica (urticaria)',  'W1237250169887', 'amber:Leve',  'green:Resuelto'),
        ('2026-08-27 10:00-05', 'Salazar Ponce, R.', 'Sospecha de reacción hemolítica','W1237250165530', 'red:Grave',   'amber:En investigación')
      ) AS e(cuando, paciente, reaccion, din, sev, estado)
      JOIN hemocomponentes h ON h.din_derivado = e.din
      JOIN unidades_sangre b ON b.id = h.unidad_matriz_id;

    -- Intercambios con otros establecimientos ---------------------------------
    INSERT INTO solicitudes_interhospitalarias (establecimiento_origen_id, establecimiento_destino_id, grupo_abo, factor_rh,
           tipo_hemocomponente, cantidad, estado, es_devolucion, creado_en, respondido_en)
    SELECT v_hlev, e.id, 'O'::grupo_abo, 'negativo'::factor_rh, 'concentrado_hematies'::tipo_hemocomponente, 4, 'pendiente'::text, false, '2026-09-29 07:50-05'::timestamptz, NULL::timestamptz
      FROM establecimientos_red e WHERE e.nombre = 'Hospital Vitarte II'
    UNION ALL
    SELECT e.id, v_hlev, 'AB'::grupo_abo, 'positivo'::factor_rh, 'plasma_fresco_congelado'::tipo_hemocomponente, 10, 'aceptada'::text, false, '2026-09-25 11:00-05'::timestamptz, '2026-09-25 15:00-05'::timestamptz
      FROM establecimientos_red e WHERE e.nombre = 'PRONAHEBAS Central';

    -- Alertas y auditoría iniciales --------------------------------------------
    INSERT INTO alertas (tipo, titulo, mensaje, creado_en) VALUES
      ('red',    '3 unidades O− vencen en menos de 24h',      'Priorizar la que vence antes — Cámara 2',       '2026-09-29 09:04-05'),
      ('amber',  'Stock de AB− bajo el mínimo de seguridad',  '9 unidades disponibles, mínimo 20',             '2026-09-29 09:03-05'),
      ('blue',   'Solicitud urgente pendiente — Emergencia',  '2 un. O− · CIE-10 S72.0 · hace 6 min',         '2026-09-29 09:02-05'),
      ('purple', '12 unidades en cuarentena serológica',      'Resultados pendientes del laboratorio',         '2026-09-29 09:01-05');

    INSERT INTO auditoria_logs (usuario_id, accion, entidad, ip_terminal, detalle, creado_en)
    SELECT u.id, a.accion, a.recurso, a.ip, jsonb_build_object('usuario', a.quien), a.cuando::timestamptz
      FROM (VALUES
        ('jjaimes@hlev.gob.pe',  'Jaimes Daza, J. (Jefe BS)',   'Autorizó despacho de unidad',                    'DIN W1237250171238',     '10.20.4.18', '2026-09-15 09:41:02-05'),
        ('jpalomino@hlev.gob.pe','Palomino Camarena, J. (T.M.)','Validó prueba de compatibilidad',                'Solicitud SOL-2026-0341','10.20.4.09', '2026-09-15 09:12:47-05'),
        ('jcangana@hlev.gob.pe', 'Cangana Salcedo, J. (Admin.)',  'Generó la etiqueta de una unidad',               'DIN W1237250171238',     '10.20.4.09', '2026-09-15 08:55:10-05'),
        ('jjaimes@hlev.gob.pe',  'Sistema (automático)',        'Bloqueó unidad por reactividad serológica',      'DIN W1237250169901',     '—',          '2026-09-14 22:03:38-05'),
        ('acamones@hlev.gob.pe', 'Camones Chávez, A. (Backend)','Reordenó el inventario por vencimiento',         'Cámara 2',               '10.20.4.03', '2026-09-14 18:20:15-05')
      ) AS a(correo, quien, accion, recurso, ip, cuando)
      JOIN usuarios u ON u.correo = a.correo;
END $$;
