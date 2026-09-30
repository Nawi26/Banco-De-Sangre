# BancoSangre HLEV — MVC (Spring Boot + Thymeleaf)

Sistema Web de Gestión de Banco de Sangre bajo el estándar ISBT 128 — Hospital de
Lima Este Vitarte (Banco de Sangre Tipo II). Esta es la primera entrega en
**Modelo-Vista-Controlador**, migrando la versión original HTML/JS de una sola página
(`index.html`) a una aplicación Spring Boot real.

**Base de datos:** PostgreSQL. Todo lo que se hace en pantalla (donantes, donaciones,
inventario, tamizaje, solicitudes, pruebas de compatibilidad, despacho, hemovigilancia,
intercambios, usuarios, alertas y bitácora) se guarda en la base y sobrevive a cerrar el
navegador y a reiniciar la aplicación.

## Cómo correrlo

Requiere Java 17+, Maven y PostgreSQL 14 o superior.

**1. Crear la base (aparte)** con `crear_base.sql`:

```bash
psql -U postgres -f crear_base.sql
```

**2. Cargar todo con un solo script** (tablas, funciones y datos de ejemplo), con el
usuario `bancosangre` (PostgreSQL 13 o superior):

```bash
psql -h localhost -U bancosangre -d bancosangre -f bancosangre_bd.sql
```

**3. (Opcional) cambiar la conexión** con variables de entorno; por defecto usa
`localhost:5432`, base `bancosangre`, usuario `bancosangre`:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/bancosangre
export DB_USER=bancosangre
export DB_PASSWORD=bancosangre
```

**4. Arrancar**

```bash
mvn spring-boot:run
```

Spring solo se conecta a la base ya creada. Si prefieres que ejecute los scripts al arrancar,
usa `DB_INICIALIZAR=true` (no duplica nada). El botón *Restablecer datos de ejemplo* usa los
scripts de `src/main/resources/db/`.

Abre `http://localhost:8080` → `/login`. Elige un rol y entra. Los usuarios de ejemplo
tienen contraseña `Banco2026` y DNI ficticios (70000001–70000007).
El Administrador tiene el botón **Restablecer datos de ejemplo** en *Administración*.

## Base de datos (`src/main/resources/db/`)

- `esquema.sql` — 15 tablas, 8 tipos ENUM, claves UUID, llaves foráneas e índices.
- `funciones.sql` — `api_listar(coleccion)` (lee) y `api_sincronizar(cambios, correo, rol)`
  (guarda todo en una sola transacción: si algo falla no se guarda nada). Aquí viven las
  reglas: la doble digitación del tamizaje debe coincidir, y solo se inserta o actualiza
  (el historial clínico nunca se borra).
- `datos.sql` — usuarios, donantes, unidades, solicitudes, etc. de ejemplo.

Ajustes respecto al diseño físico original (marcados con `AJUSTE` en `esquema.sql`):
estado de unidad `fraccionado`; `fecha_nacimiento`, `paciente_dni`, `din_check_digit` y
`donacion_id` aceptan nulos (la pantalla aún no los pide); algunas columnas de texto extra
en solicitudes y establecimientos.

## Limitaciones conocidas

- El inicio de sesión sigue siendo permisivo: guarda el rol elegido, no valida la
  contraseña contra la base (las contraseñas ya están cifradas con bcrypt en `usuarios`).
- Las existencias por grupo sanguíneo del Dashboard, las campañas y la tabla de stock de
  la red interhospitalaria todavía son datos fijos.
- Cada pantalla guarda las listas completas que cargó: si dos personas editan lo mismo a
  la vez, gana el último en guardar. Si un guardado falla, la pantalla avisa y se recarga.

## Qué es Modelo, Vista y Controlador aquí

- **Modelo** (`src/main/java/.../model/`)
  - `Rol.java` — los 4 roles del sistema (RF-02) y qué vistas puede ver cada uno
    (reemplaza al `ROLE_VIEWS` que antes vivía en JavaScript).
  - `Vista.java` — catálogo de las 12 pantallas (clave, título, subtítulo).
- **Controlador** (`src/main/java/.../controller/`)
  - `AuthController.java` — `/login` (RF-01) y `/logout`. Guarda el rol elegido
    en la sesión HTTP.
  - `AppController.java` — una ruta real por pantalla (`/dashboard`,
    `/inventario`, `/donantes`, …). Antes la navegación la resolvía
    `showView()` en JavaScript dentro de una sola página; ahora cada clic en el
    menú es una petición HTTP real, y el Controlador decide (con el RBAC de
    RF-02) si el rol puede ver esa vista o si lo redirige de vuelta al
    dashboard.
- **Vista** (`src/main/resources/templates/`)
  - `fragments/layout.html` — el "shell" compartido: sidebar, topbar, modal y
    toasts. Lo reutilizan las 12 pantallas mediante fragmentos de Thymeleaf.
  - `login.html` + una plantilla por pantalla (`dashboard.html`,
    `inventario.html`, `donantes.html`, …) — el contenido visual es el mismo
    HTML de la versión original, ahora servido por el backend en vez de
    alternarse con JavaScript.
  - `static/css/styles.css` y `static/js/app.js` — el mismo CSS y las mismas
    funciones de interacción (tablas, modales, tabs, toasts) de la versión original;
    solo se quitó el router de una sola página (`showView`, `enterApp`,
    `exitApp`), porque ahora la navegación la maneja Spring.

## Qué falta
- Autenticación real (validar contraseña con `crypt()` y bloquear cuentas desactivadas).
- Pasar `api_sincronizar` a servicios por entidad y agregar pruebas automáticas.

## Correspondencia con la versión original

Referencia visual original: `index.html` (mismo diseño, los 38
RF renumerados RF-01–RF-38). Este proyecto reproduce las 13 pantallas
(login + 12) con el mismo HTML/CSS, reestructurado en capas MVC.
