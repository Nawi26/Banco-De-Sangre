# Sistema Web de Gestión de Banco de Sangre — estándar ISBT 128

Proyecto académico (UTP) para el Hospital de Lima Este Vitarte (HLEV).
El repositorio tiene dos partes bien separadas:

| Carpeta | Qué es | Tecnología |
|---|---|---|
| [`frontend/`](frontend) | **Frontend**: pantallas de la aplicación. No necesita servidor ni base de datos; se abre en el navegador. | HTML, CSS, JavaScript |
| [`mvc-springboot/`](mvc-springboot) | **Aplicación MVC completa**: pantallas + lógica + base de datos PostgreSQL. | Spring Boot 3, Thymeleaf, JdbcTemplate, PostgreSQL |

## Arquitectura MVC (carpeta `mvc-springboot`)

| Capa | Dónde está | Qué hace |
|---|---|---|
| **Modelo** | `src/main/java/.../model`, `db/Repositorio.java`, `src/main/resources/db/*.sql` | Roles, vistas y acceso a PostgreSQL (tablas y funciones `api_listar` / `api_sincronizar`). |
| **Vista** | `src/main/resources/templates/*.html`, `static/css`, `static/js` | Pantallas Thymeleaf, estilos y JavaScript (`app.js`, `validacion.js`). |
| **Controlador** | `src/main/java/.../controller` | `AuthController` (login), `AppController` (pantallas según el rol), `ApiController` (guardado de datos). |
| **Validación** | `static/js/validacion.js` (navegador) y `ValidadorDatos.java` (servidor) | DNI de 8 dígitos, nombres solo con letras, fechas `dd/mm/aaaa`, HC, CIE-10, etc. |

## Cómo ejecutar el MVC

1. Instalar JDK 17, Maven y PostgreSQL.
2. Crear la base de datos `bancosangre` (pgAdmin o `psql`).
3. Crear el archivo `mvc-springboot/src/main/resources/application-local.properties` con tu usuario y clave de
   PostgreSQL (este archivo está en `.gitignore`, no se sube a GitHub):
   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/bancosangre
   spring.datasource.username=postgres
   spring.datasource.password=TU_CLAVE
   app.db.inicializar=true
   ```
   Con `inicializar=true` la aplicación crea sola las tablas, funciones y datos de ejemplo al arrancar
   (no duplica ni borra lo ya guardado).
4. Ejecutar:
   ```
   cd mvc-springboot
   mvn spring-boot:run
   ```
5. Abrir http://localhost:8080/login

## Cómo ver solo el frontend

Abrir `frontend/index.html` en el navegador.
