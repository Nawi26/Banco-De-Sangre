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
