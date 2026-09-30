-- =====================================================================
--  Actualiza las 7 cuentas del equipo en una base que YA tiene datos
--  (la carga inicial solo se ejecuta cuando la base está vacía).
--  Pone los roles (Cangana y Yangali = Administrador), activa las cuentas y
--  asigna las contraseñas nuevas. Se puede ejecutar varias veces.
--  Uso (pgAdmin: Query Tool sobre la base bancosangre) o:
--    psql -U bancosangre -d bancosangre -f actualizar_usuarios_equipo.sql
-- =====================================================================
INSERT INTO usuarios (dni, correo, contrasena_hash, nombres, apellidos, rol) VALUES
      ('70000001', 'jcangana@hlev.gob.pe', '$2a$10$AKGEzh98UOY.mlWQEGAeIufDv/dljnCgebtMOXLCRrYeqqOIPmR0a', 'José Leonel', 'Cangana Salcedo', 'administrador'),
      ('70000006', 'jyangali@hlev.gob.pe', '$2a$10$dlWOsAHoDbi.k/4y0n9f4Ob3FpVWZ2rn8sIofeyCK6t1UgpacyeiS', 'Jesús Alberto', 'Yangali Saravia', 'administrador'),
      ('70000003', 'jjaimes@hlev.gob.pe', '$2a$10$8/WsFyOrSDd1sPwbXLMytOW6V1LE9hoCikRugBQ5GdBP01HefsoZy', 'Julibeth Antonela', 'Jaimes Daza', 'jefe_banco_sangre'),
      ('70000002', 'jpalomino@hlev.gob.pe', '$2a$10$2//2nKOC7Wv3fM.Re4J3UuGEEJLsIl1cVIZA.4bKc.FuegpHXjiPC', 'José William', 'Palomino Camarena', 'tecnologo_medico'),
      ('70000005', 'acamones@hlev.gob.pe', '$2a$10$.OKaCWzzyThFvZh5Vzz3t.0bs1FT6N8QORFL71Vy2lE8M1c8gMhma', 'Antony David', 'Camones Chávez', 'tecnologo_medico'),
      ('70000007', 'aaylas@hlev.gob.pe', '$2a$10$tY5pa2R1mLclh92zSwux9.FmEHO4LhwgXDVOucYTkA0rcazwFO/Te', 'Alexander Hernan', 'Aylas Valdez', 'medico_solicitante'),
      ('70000004', 'dbatallanos@hlev.gob.pe', '$2a$10$p3zcvDp9Fzue3Zns8DVDJes4D.LeZF3BOqHz36F8S9PxBoWnV7GtC', 'Daniel Enrique', 'Batallanos Ibarra', 'medico_solicitante')
ON CONFLICT (correo) DO UPDATE
   SET contrasena_hash = EXCLUDED.contrasena_hash,
       nombres         = EXCLUDED.nombres,
       apellidos       = EXCLUDED.apellidos,
       rol             = EXCLUDED.rol,
       activo          = true,
       actualizado_en  = now();
