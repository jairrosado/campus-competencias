# Thunder Client · API Campus Competencias

Colección lista para probar todos los endpoints de la API sin escribir las solicitudes a mano.

| Archivo | Uso |
| --- | --- |
| `campus-competencias.postman_collection.json` | Colección completa: 44 solicitudes en 11 carpetas. |
| `campus-competencias.env` | Entorno recomendado para importar en la pestaña **Env**. |
| `campus-competencias.postman_environment.json` | El mismo entorno en formato Postman, por si prefieres ese archivo. |
| `generar-thunder.mjs` | Regenera la colección y el entorno después de cambiar cuerpos o rutas. |

La colección usa el formato **Postman 2.1**, que Thunder Client importa de forma oficial. No modifica el backend ni el JSON de la API.

## 1. Antes de empezar

1. MongoDB encendido y el backend corriendo:

   ```powershell
   npm run dev
   ```

2. Comprueba en el navegador o en Thunder Client:

   ```text
   GET http://localhost:3001/api/health
   ```

   Debe responder `200` con `{"data":{"status":"ok","database":true}}`.

## 2. Importar

**Colección**

1. En VS Code abre el panel de **Thunder Client**.
2. Pestaña **Collections** → menú `⋯` → **Import**.
3. Selecciona `thunder/campus-competencias.postman_collection.json`.

**Entorno** (necesario para que funcionen `{{baseUrl}}`, `{{csrfToken}}`, etc.)

1. Pestaña **Env** → **Import**.
2. Selecciona `thunder/campus-competencias.env`.
3. Activa el entorno **Campus Competencias · Local** en el selector superior.

## 3. Ajustar variables

| Variable | Valor inicial | Qué hacer |
| --- | --- | --- |
| `baseUrl` | `http://localhost:3001` | Servidor local. Para el emulador usa `http://10.0.2.2:3001`; para un celular, la IP LAN del PC. |
| `correo` | `admin@campus.edu.co` | Correo de una cuenta **existente** en tu base. |
| `password` | `Campus2026*` | Contraseña real de esa cuenta. |
| `csrfToken` | vacío | Se llena con la respuesta de **Login** o **Me**. |
| `usuarioId` | `000…` | Id del usuario autenticado. **Login** lo guarda solo. |
| `organizadorId` | `000…` | Id de un organizador de prueba. |
| `participanteUsuarioId` | `000…` | Cuenta con rol `participante`. |
| `institucionId` | `000…` | Id de una institución. |
| `disciplinaId` | `000…` | Id de una disciplina. |
| `categoriaId` | `000…` | Id de una categoría **de esa disciplina**. |
| `torneoId` | `000…` | Id de un torneo en estado `inscripciones`. |
| `hackatonId` | `000…` | Id de un hackatón. |
| `participanteId` | `000…` | Id de un participante activo. |
| `equipoId` | `000…` | Id de un equipo deportivo. |
| `equipoTecnologicoId` | `000…` | Id de un equipo tecnológico. |

Los `000…` son marcadores: las rutas con `:id` responden `400` hasta que pongas un id real. Puedes obtenerlos ejecutando las solicitudes **listar** y copiando `data.items[].id`.

Si usas un celular, cambia `baseUrl` por la IP que muestra la consola del backend, por ejemplo `http://192.168.1.10:3001`, y permite Node.js en el Firewall de Windows para redes privadas.

## 4. Cookie y CSRF (paso clave)

La API usa dos protecciones:

- **Cookie `campus_session`**: es `HttpOnly`. Thunder Client la guarda y la reenvía automáticamente después del login; no tienes que copiarla.
- **Header `X-CSRF-Token`**: obligatorio en `POST`, `PUT`, `PATCH` y `DELETE`. Sin él, la API responde `403 CSRF`.

**Forma manual (funciona siempre):**

1. Ejecuta **01 · Autenticación → Login**.
2. En la respuesta busca `data.csrfToken`.
3. Pega ese valor en la variable `csrfToken` del entorno.
4. Ejecuta cualquier solicitud de escritura.

El token dura lo que dura la sesión. Si ves `403 CSRF`, vuelve a ejecutar **Me · sesión actual** y copia el `csrfToken` nuevo.

**Forma automática:** algunas solicitudes incluyen un script de prueba que guarda `csrfToken`, `usuarioId`, `institucionId`, `disciplinaId`, `categoriaId`, etc. Los scripts de Thunder Client requieren la versión de pago; si no están disponibles, ignora los avisos y usa el paso manual. Los scripts no bloquean la solicitud.

## 5. Flujo recomendado para crear datos de prueba

Este orden evita errores de referencias inexistentes. Los nombres son los de la colección.

| Paso | Solicitud | Variable que llena |
| --- | --- | --- |
| 1 | `Login` | `csrfToken`, `usuarioId` |
| 2 | `Instituciones · crear` | `institucionId` |
| 3 | `Usuarios · crear participante` | `participanteUsuarioId` |
| 4 | `Disciplinas · crear` | `disciplinaId`, `categoriaId` |
| 5 | `Torneos · crear` | `torneoId` |
| 6 | `Torneos · abrir inscripciones` | — (deja el torneo en `inscripciones`) |
| 7 | `Participantes · crear` | `participanteId` |
| 8 | `Equipos · crear deportivo` | `equipoId` |
| 9 | `Inscripciones · crear individual` | — |

Para el flujo de hackatón: `Hackatones · crear` → `Equipos · crear tecnológico` → `Hackatones · abrir inscripciones` (cuando ya no necesites crear equipos en borrador).

Si ya tienes datos en la base, salta los pasos de creación: ejecuta las solicitudes **listar** y copia los ids en el entorno.

> **Inscripciones:** el torneo debe estar en estado `inscripciones` **y** la fecha actual debe estar entre `inscripcionInicio` e `inscripcionFin`. El torneo de ejemplo usa una ventana abierta (octubre–diciembre de 2026). Fuera de esa ventana la API responde `409 REGISTRATION_CLOSED`.

## 6. Endpoints incluidos

Roles: **Público** (sin sesión), **Sesión** (cualquier usuario autenticado), **Admin**, **Admin/Org** (administrador u organizador), **Admin/Org/Participante**.

### 00 · Salud y setup

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/health` | Público | No |
| GET | `/api/setup/status` | Público | No |
| POST | `/api/setup/administrador` | Público (solo si `needsSetup`) | No |

### 01 · Autenticación

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Público | No |
| GET | `/api/auth/me` | Sesión | No |
| POST | `/api/auth/logout` | Sesión | Sí |

### 02 · Usuarios

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/usuarios?page=1&limit=10` | Admin | No |
| POST | `/api/usuarios` | Admin | Sí |
| PATCH | `/api/usuarios/me` | Sesión | Sí |
| GET | `/api/usuarios/:id` | Admin o el propio usuario | No |
| PATCH | `/api/usuarios/:id` | Admin | Sí |

### 03 · Instituciones

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/instituciones?page=1&limit=10` | Sesión | No |
| POST | `/api/instituciones` | Admin | Sí |
| GET | `/api/instituciones/:id` | Sesión | No |
| PUT | `/api/instituciones/:id` | Admin | Sí |
| PATCH | `/api/instituciones/:id` | Admin | Sí |
| DELETE | `/api/instituciones/:id` | Admin | Sí |

### 04 · Catálogos

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/catalogos` | Sesión | No |

### 05 · Disciplinas

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/disciplinas?page=1&limit=10` | Sesión | No |
| GET | `/api/disciplinas/:id` | Sesión | No |
| POST | `/api/disciplinas` | Admin | Sí |
| PATCH | `/api/disciplinas/:id` | Admin | Sí |

### 06 · Torneos

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/torneos?page=1&limit=10` | Sesión | No |
| GET | `/api/torneos/:id` | Sesión | No |
| POST | `/api/torneos` | Admin/Org | Sí |
| PATCH | `/api/torneos/:id` | Admin/Org responsable | Sí |
| PATCH | `/api/torneos/:id/estado` | Admin/Org responsable | Sí |

### 07 · Hackatones

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/hackatones?page=1&limit=10` | Sesión | No |
| GET | `/api/hackatones/:id` | Sesión | No |
| POST | `/api/hackatones` | Admin/Org | Sí |
| PATCH | `/api/hackatones/:id` | Admin/Org responsable | Sí |
| PATCH | `/api/hackatones/:id/estado` | Admin/Org responsable | Sí |

### 08 · Participantes

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/participantes?page=1&limit=10` | Sesión (según alcance) | No |
| GET | `/api/participantes/:id` | Sesión (según alcance) | No |
| POST | `/api/participantes` | Admin/Org | Sí |
| PATCH | `/api/participantes/:id` | Admin/Org | Sí |

### 09 · Equipos

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/equipos?page=1&limit=10` | Sesión (según alcance) | No |
| GET | `/api/equipos/:id` | Sesión (según alcance) | No |
| POST | `/api/equipos` | Admin/Org/Participante | Sí |
| PATCH | `/api/equipos/:id` | Admin/Org/Participante responsable | Sí |

### 10 · Inscripciones

| Método | Ruta | Rol | CSRF |
| --- | --- | --- | --- |
| GET | `/api/inscripciones?page=1&limit=10` | Sesión (según alcance) | No |
| POST | `/api/inscripciones` | Admin/Org/Participante | Sí |

## 7. Reglas que suelen generar errores

- **No envíes campos extra.** La API rechaza cualquier propiedad fuera de la lista permitida con `400 FORBIDDEN_FIELDS`. Por eso los cuerpos de equipo tecnológico no incluyen `institucion`, `disciplina`, `categoria` ni `modalidad`.
- **`PUT /api/instituciones/:id` exige los cinco campos**: `nombre`, `ciudad`, `departamento`, `tipo` y `estado`. Para cambios parciales usa `PATCH`.
- **Fechas ISO 8601 con zona horaria**, por ejemplo `2027-01-15T08:00:00.000-05:00`. El orden es `inscripcionInicio ≤ inscripcionFin ≤ inicio ≤ fin`. En hackatones además: `entregaInicio ≤ entregaFin ≤ evaluacionInicio ≤ evaluacionFin`.
- **Referencias activas**: institución, disciplina y participante deben estar `activo` para crear torneos, equipos e inscripciones.
- **`categoria`** en torneos y equipos deportivos es el id de una categoría de la disciplina, no su nombre. En participantes, `categoria` sí es texto libre.
- **Roles**: `participante` debe ser una cuenta activa con rol `participante`; `responsable` debe ser administrador u organizador en torneos y hackatones.
- **Estados de torneo**: `borrador → inscripciones` es la única transición expuesta por la API en esta entrega. Con el evento abierto, el reglamento y el formato quedan bloqueados.
- **Equipos tecnológicos**: no pueden crearse si el hackatón está `publicado` o `cancelado`, y la plantilla debe respetar `integrantesMin`/`integrantesMax`.

## 8. Errores frecuentes

| Respuesta | Significado | Solución |
| --- | --- | --- |
| `401 UNAUTHENTICATED` | No hay sesión o expiró. | Ejecuta **Login** otra vez. |
| `403 CSRF` | Falta el header o el token no coincide. | Copia `data.csrfToken` de **Login**/**Me** en `csrfToken`. |
| `403 FORBIDDEN` | El rol no permite la operación o no gestionas el registro. | Inicia sesión con un rol adecuado. |
| `400 REFERENCE` / `INACTIVE_REFERENCE` | Un id no existe o el registro está inactivo. | Revisa los ids del entorno y activa la referencia. |
| `400 FORBIDDEN_FIELDS` | Enviaste una propiedad no permitida. | Elimina el campo del cuerpo. |
| `409 DUPLICATE` | Ya existe el registro (correo, institución, equipo, etc.). | Usa otro valor o consulta el existente. |
| `409 REGISTRATION_CLOSED` | El torneo no está en ventana de inscripción. | Abre inscripciones y ajusta las fechas. |
| `409 INSTITUTION_IN_USE` | La institución tiene participantes, equipos o inscripciones. | Desactívala con `PATCH` en lugar de borrarla. |
| `429 RATE_LIMIT` | Más de 10 intentos de login por minuto. | Espera un minuto. |
| `503` en `/api/health` | MongoDB no está conectado. | Enciende MongoDB y reinicia el backend. |

La forma de los errores es siempre:

```json
{ "error": { "code": "CSRF", "message": "La solicitud no es válida. Actualiza la página." } }
```

## 9. Regenerar la colección

Si cambias cuerpos, nombres o rutas en `generar-thunder.mjs`:

```powershell
node thunder/generar-thunder.mjs
```

El script reescribe los tres archivos y valida que el JSON se pueda leer. Después vuelve a importar la colección en Thunder Client (o elimínala antes para evitar duplicados).
