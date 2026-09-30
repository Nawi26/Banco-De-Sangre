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
