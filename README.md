# Campus: gestión de competencias

## Desplegar la API y la web en Render

El archivo `render.yaml` configura un único servicio web que compila la web con Vite y la sirve junto con la API. Así la plataforma completa funciona con una sola URL, sin depender de un `node` encendido en tu equipo y sin problemas de cookies entre dominios. Antes de desplegar, crea una base de datos MongoDB accesible desde internet (por ejemplo, en MongoDB Atlas) y permite las conexiones desde Render en la lista de acceso de red del proveedor. No uses la URI local `127.0.0.1` en producción.

En Render, crea un **Blueprint** conectado a este repositorio y confirma el servicio `campus-competencias-api`. Cuando Render lo solicite, configura:

- `MONGODB_URI`: URI de conexión de producción, incluida la base de datos `torneos_universidad`.
- `CLIENT_ORIGIN`: solo es necesario si la web se aloja en otro dominio. En ese caso usa el origen exacto, por ejemplo `https://campus.example.com`, sin ruta final. Si el mismo servicio sirve la web, puede quedar con el valor temporal `https://example.com`.

Render ejecutará `npm ci --include=dev && npm run build` (instala dependencias, compila la web en `frontend/dist`) y luego `npm run start --workspace backend`. `PORT` lo asigna Render automáticamente y el backend escucha en `0.0.0.0`.

Para verificar el servicio:

- `https://<nombre-del-servicio>.onrender.com/` abre la plataforma web.
- `https://<nombre-del-servicio>.onrender.com/api/health` debe responder `status: "ok"` y `database: true`.

Si el servicio ya existía y solo mostraba la API, actualiza en **Settings → Build & Deploy** el *Build Command* a `npm ci --include=dev && npm run build` y el *Start Command* a `npm run start --workspace backend`, guarda y vuelve a desplegar. Si Render no encuentra `frontend/dist`, la raíz responderá con un JSON que indica que la web no está compilada.

El plan gratuito de Render suspende el servicio tras unos minutos sin tráfico; la primera visita después de eso puede tardar cerca de un minuto mientras vuelve a arrancar.

La configuración de producción desactiva la creación pública de la cuenta inicial de administrador. Para crearla después del despliegue, activa `ALLOW_INITIAL_SETUP=true` temporalmente en las variables del servicio, reinícialo, crea la cuenta desde la aplicación y vuelve a desactivarla. Mantén `MONGODB_URI` como variable secreta y usa una contraseña fuerte para esa cuenta.

Plataforma de torneos, intercolegiados y hackatones. API REST con Node.js, Express y Mongoose sobre MongoDB; interfaz React con Vite, HTML y CSS.

## Requisitos

- Node.js 22.12 o superior y npm.
- Servidor MongoDB iniciado. La conexión local predeterminada usa el puerto 27017.

## Instalación y ejecución

En una copia nueva del repositorio, abre una terminal en la raíz e instala las dependencias:

```powershell
npm ci
```

Crea `backend/.env` a partir de `backend/.env.example`. En PowerShell:

```powershell
Copy-Item backend/.env.example backend/.env
```

En macOS o Linux, el comando equivalente es `cp backend/.env.example backend/.env`. Ajusta `MONGODB_URI` si el servidor MongoDB utiliza otra conexión. El archivo `.env` es local y está excluido de Git.

Inicia la API y la interfaz:

```powershell
npm run dev
```

Abre [la plataforma local](http://127.0.0.1:5173). La API usa el puerto 3001 y la base predeterminada es `torneos_universidad`. En el primer acceso se crea la cuenta de administrador desde la interfaz. Detén ambos procesos con **Ctrl+C**.

## Alcance actual

Acceso, cuentas, perfil, instituciones, disciplinas, torneos, configuración de hackatones, reglamentos, participantes, equipos, catálogo e inscripciones deportivas. Corresponde a 13 historias implementadas de la matriz vigente.

Escenarios, demás etapas, inscripción a hackatones, agenda, resultados, proyectos y evaluación están pendientes. La entrega actual se ejecuta localmente con una instancia de API; los cupos concurrentes requieren coordinación compartida si se despliega con varias instancias.

## Dónde está cada parte

| Carpeta o archivo | Contenido |
| --- | --- |
| `backend/src` | API, modelos de la base, validaciones, permisos y conexión MongoDB. |
| `backend/tests` | Pruebas de integración con bases temporales. |
| `frontend/src` | Pantallas, formularios, solicitudes a la API y estilos. |
| `frontend/index.html` y `frontend/vite.config.js` | Entrada web y configuración de desarrollo. |
| `android/` | Aplicación nativa Android con Kotlin y Jetpack Compose que consume la misma API. |
| `thunder/` | Colección de Thunder Client con todos los endpoints, entorno local y guía de uso. |
| `package.json` y `package-lock.json` | Comandos y versiones de las dependencias. |
| `documentacion` | Material académico local, excluido de Git. |

Para la base de datos, empieza por [los modelos](backend/src/models.js) y [los modelos de competencias](backend/src/competition-models.js). La conexión está en [server.js](backend/src/server.js).

El código del backend, la web y Android lleva comentarios de trazabilidad: cada archivo indica qué historias de usuario implementa y los bloques clave llevan `// HU-XX`.

## Comprobar

```powershell
npm test
npm run build
```

Las pruebas requieren MongoDB y crean bases temporales separadas de la aplicación. La compilación genera `frontend/dist`.

## Probar la API con Thunder Client

La carpeta `thunder/` incluye una colección con las 44 solicitudes de la API y un entorno local. Importa `thunder/campus-competencias.postman_collection.json` desde **Collections → Import** y `thunder/campus-competencias.env` desde **Env → Import**. Ajusta `baseUrl`, `correo` y `password`, ejecuta **Login** y copia `data.csrfToken` en la variable `csrfToken` antes de las escrituras.

El flujo completo, la tabla de variables, los roles por endpoint y los errores frecuentes están en [thunder/README.md](thunder/README.md).

## Aplicación Android

La carpeta `android/` contiene una app nativa en Kotlin con Jetpack Compose y Material 3. Consume la misma API sin modificar endpoints, modelos ni estructura del JSON: incluye inicio de sesión y configuración inicial, instituciones (listar, crear, editar, eliminar o desactivar), cuentas de estudiante, torneos, hackatones, disciplinas, participantes, equipos, inscripciones y perfil, con formularios de alta y edición para el administrador.

Abre `android/` en Android Studio con el backend encendido, o compila desde la terminal:

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

El backend escucha en `0.0.0.0` por defecto, así que el mismo servidor atiende al emulador y a un celular conectado a la misma red Wi-Fi. Al arrancar, la consola muestra las direcciones disponibles:

```text
Equipo:            http://localhost:3001
Celular (Wi-Fi):   http://192.168.1.10:3001
Emulador Android:  http://10.0.2.2:3001
```

La app detecta el entorno en la primera ejecución: emulador Android (`http://10.0.2.2:3001/`) o dispositivo físico (`http://192.168.80.40:3001/`). La dirección puede cambiarse desde **Cambiar servidor** y permanece guardada en el dispositivo. También puede fijarse mediante `CAMPUS_API_BASE_URL` o `VITE_API_URL` en Gradle/variables de entorno. Este modo es para desarrollo local: no expongas la API a redes públicas.

La web centraliza sus solicitudes en `frontend/src/api.js`; en desarrollo, Vite las envía al backend en `http://localhost:3001`. Para usar otra API, configura `VITE_API_URL` en `frontend/.env` (ver `frontend/.env.example`). Si se usa una URL directa desde el navegador, configura `CLIENT_ORIGIN` en `backend/.env` con el origen exacto de la web para habilitar cookies y solicitudes autenticadas.

Si prefieres fijar la URL desde la compilación:

```powershell
.\gradlew.bat :app:installDebug -PCAMPUS_API_BASE_URL=http://192.168.1.10:3001/
```

La arquitectura, el mapeo de los DTO, la autenticación con cookie y CSRF, y la solución de problemas están en [android/README.md](android/README.md).

## CRUD con Mongoose

El recurso de instituciones permite demostrar los cuatro métodos solicitados en la materia. Las rutas están en `backend/src/app.js` y el modelo en `backend/src/models.js`.

| Método | Ruta | Operación con Mongoose |
| --- | --- | --- |
| GET | `/api/instituciones` | Listar con `find`, filtros y paginación. |
| GET | `/api/instituciones/:id` | Consultar con `findById`. |
| POST | `/api/instituciones` | Registrar con `create`. |
| PUT | `/api/instituciones/:id` | Actualizar todos los campos editables con `findByIdAndUpdate`. |
| DELETE | `/api/instituciones/:id` | Eliminar con `findByIdAndDelete`, si no tiene relaciones. |

`PATCH` sigue disponible para cambios parciales. Las consultas requieren una sesión; las escrituras requieren administrador y token CSRF. El formulario web de edición de instituciones utiliza `PUT`.

Desde la pantalla **Instituciones**, el administrador puede usar el lápiz para editar o la papelera para eliminar. La eliminación solicita confirmación y actualiza el directorio y sus contadores. Si hay participantes, equipos o inscripciones vinculados, se conserva la institución y se ofrece desactivarla. El campo **Ciudad/municipio** mantiene la propiedad `ciudad` en MongoDB y en la API.

Para probarlo desde un cliente de API:

1. Inicia sesión con `POST http://127.0.0.1:3001/api/auth/login`, enviando tu `correo` y `password` en JSON. El cliente debe conservar la cookie de sesión.
2. Copia `data.csrfToken` de la respuesta y envíalo en el encabezado `X-CSRF-Token` de las solicitudes POST, PUT y DELETE de instituciones.
3. Crea una institución con POST y guarda el `data.id` devuelto. Utiliza ese identificador para GET, PUT y DELETE.

Ejemplo de cuerpo para POST y PUT:

```json
{
  "nombre": "Colegio de demostración",
  "ciudad": "Bogotá",
  "departamento": "Cundinamarca",
  "tipo": "colegio",
  "estado": "activo"
}
```

PUT exige los cinco campos. DELETE no necesita cuerpo; elimina únicamente una institución sin participantes, equipos ni inscripciones relacionadas. Si tiene historial, responde 409 y permite conservarlo desactivando la institución. Estas reglas están comprobadas en las pruebas de integración, incluidas solicitudes simultáneas.

## Visual Studio Code

Abre `Campus.code-workspace` para una vista centrada en el código. Para consultar los documentos desde el explorador, abre la carpeta completa del repositorio. Las dependencias y archivos generados se ocultan mediante `.vscode/settings.json`.

## Archivos locales

El repositorio incluye el código de `backend` y `frontend`, pruebas, configuración de ejemplo, comandos e instrucciones de instalación. La carpeta `documentacion` se conserva solo en la copia local y no se necesita para ejecutar el sistema.

Las dependencias, compilaciones y el archivo privado `backend/.env` también están excluidos de Git. Quien clone el proyecto debe instalar las dependencias, crear su propio `.env` e iniciar MongoDB, siguiendo los pasos anteriores. Los datos guardados en MongoDB no forman parte del repositorio.
