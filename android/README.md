# Campus Competencias · Android

Aplicación nativa Android construida con **Kotlin + Jetpack Compose + Material 3** sobre la misma API REST del proyecto web. No se modificaron los endpoints, los modelos de MongoDB ni la estructura del JSON: la app únicamente consume los recursos existentes y traduce las respuestas a modelos tipados de Kotlin. Por defecto apunta a la API publicada en Render, así que funciona sin encender un backend local.

## Requisitos

- Android Studio reciente (AGP 9.3.2, Gradle 9.5.0).
- JDK 17 o superior (Android Studio incluye su propio JBR).
- SDK de Android 37 instalado (`compileSdk`/`targetSdk` 37, `minSdk` 26).
- Conexión a internet (la API por defecto está en la nube).
- Para desarrollo local: backend y MongoDB del proyecto encendidos.

## Usar la API en la nube (recomendado)

La app usa por defecto:

```text
https://campus-competencias-api.onrender.com/
```

Esa URL sirve la API y la web en el mismo origen. No requiere configuración: instala el APK, abre la app y comienza a usarla desde cualquier red.

> El plan gratuito de Render suspende el servicio cuando pasa un tiempo sin actividad. La primera petición puede tardar cerca de un minuto; espera la respuesta en la pantalla de conexión.

## Ejecutar el backend local (desarrollo)

Desde la raíz del repositorio:

```powershell
npm ci
npm run dev
```

El backend escucha en `0.0.0.0:3001` por defecto (`HOST=0.0.0.0` en `backend/.env`). Así el mismo servidor atiende al emulador y a un celular conectado a la misma red Wi-Fi. Al arrancar, la consola muestra las direcciones disponibles:

```text
API escuchando en 0.0.0.0:3001
  Equipo:            http://localhost:3001
  Celular (Wi-Fi):   http://192.168.1.10:3001
  Emulador Android:  http://10.0.2.2:3001
```

La IP del celular es la del equipo en la red local; puede cambiar si te conectas a otra red. Si la consola muestra varias IP (por ejemplo, adaptadores virtuales), usa la de tu conexión Wi-Fi. Este modo es para desarrollo local: no expongas la API a redes públicas.

## Abrir y ejecutar la app

1. Abre la carpeta `android/` en Android Studio.
2. Espera la sincronización de Gradle.
3. Elige un emulador o un celular conectado y ejecuta la configuración `app`.

`ServerSettings` centraliza la URL. Si no hay una preferencia guardada, usa `ServerSettings.CLOUD_URL` (`https://campus-competencias-api.onrender.com/`). El emulador (`http://10.0.2.2:3001/`) y la IP local son atajos opcionales para desarrollo. Se puede sobrescribir con la propiedad Gradle `CAMPUS_API_BASE_URL` o `VITE_API_URL` (también como variable de entorno); el valor se inyecta en `BuildConfig.API_BASE_URL`.

Direcciones disponibles:

```text
Nube (predeterminada): https://campus-competencias-api.onrender.com/
Emulador:              http://10.0.2.2:3001/
Celular (Wi-Fi):       http://192.168.1.10:3001/
```

`10.0.2.2` es la forma en que el emulador de Android ve el equipo anfitrión.

### Cambiar de servidor sin recompilar

La app guarda la dirección de la API en el dispositivo (`SharedPreferences`) y la aplica a cada solicitud mediante un interceptor de OkHttp. Puedes cambiarla desde:

- **Login / configuración inicial**: botón con la dirección actual al pie del formulario.
- **Error de conexión**: botón **Cambiar servidor**.
- **Mi perfil → Conexión**: botón **Cambiar servidor**.

El diálogo acepta `192.168.1.10:3001`, `http://192.168.1.10:3001` o una URL completa. Incluye los atajos **Nube** y **Emulador**, y valida la dirección antes de guardar. Al cambiar de servidor se cierra la sesión local porque la cookie pertenece al servidor anterior.

### Emulador

Abre **Cambiar servidor → Emulador** para usar `http://10.0.2.2:3001/` con el backend local. Para volver a la nube usa **Cambiar servidor → Nube**.

### Dispositivo físico

Con la dirección por defecto no necesitas nada más: instala el APK y usa la app con internet.

Si prefieres el backend local:

1. Conecta el celular y el computador a la misma red Wi-Fi.
2. Inicia el backend con `npm run dev`. La consola imprime la IP que debe usar el celular.
3. Abre **Cambiar servidor** y escribe la IP del equipo, por ejemplo `http://192.168.1.10:3001`.
4. Permite el puerto 3001 en el firewall de Windows. En PowerShell como administrador:

   ```powershell
   New-NetFirewallRule -DisplayName "Campus API 3001" -Direction Inbound -Protocol TCP -LocalPort 3001 -Action Allow
   ```

5. Si sigue sin conectar, revisa que la red de Windows esté marcada como **Privada** y que el router no tenga activado el aislamiento de clientes (AP isolation).

También puedes fijar la dirección en tiempo de compilación:

```powershell
cd android
.\gradlew.bat :app:installDebug -PCAMPUS_API_BASE_URL=http://192.168.1.10:3001/
```

o añadirla a `android/gradle.properties`:

```properties
CAMPUS_API_BASE_URL=http://192.168.1.10:3001/
```

La app permite tráfico HTTP hacia el backend local mediante `res/xml/network_security_config.xml`; la URL en la nube usa HTTPS.

## Compilar desde la terminal

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

El APK se genera en `android/app/build/outputs/apk/debug/app-debug.apk`.

```powershell
.\gradlew.bat :app:installDebug
```

Si Gradle no encuentra Java, define `JAVA_HOME` apuntando al JBR de Android Studio y `ANDROID_HOME` al SDK:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
```

## Arquitectura

```text
android/app/src/main/java/com/campus/competencias/
├── CampusApplication.kt          Application + AppContainer
├── MainActivity.kt               Host Compose + edge-to-edge
├── data/
│   ├── model/                    DTO de kotlinx.serialization
│   │   ├── AuthModels.kt         UserDto, SessionDto, LoginRequest...
│   │   ├── CommonModels.kt       ApiEnvelope, PageDto, SummaryDto...
│   │   ├── CompetitionModels.kt  Torneos, hackatones, disciplinas...
│   │   ├── AdminModels.kt        Cuerpos POST/PATCH de cuentas y competencias
│   │   └── InstitutionModels.kt  InstitutionDto, InstitutionInput...
│   ├── remote/
│   │   ├── CampusApi.kt          Interfaz Retrofit: solo endpoints existentes
│   │   ├── ApiClient.kt          OkHttp + Retrofit + interceptor de URL y CSRF
│   │   ├── ApiExecutor.kt        Traduce errores HTTP al formato de la API
│   │   ├── ServerSettings.kt     URL de la API guardada en el dispositivo
│   │   ├── SessionCookieJar.kt   Cookie campus_session persistente
│   │   └── SessionManager.kt     Token CSRF y expiración de sesión
│   └── repository/
│       ├── AuthRepository.kt     setup, login, me, logout
│       └── CampusRepository.kt   listados y CRUD de todos los recursos
├── di/AppContainer.kt            Inyección manual, sin Hilt
└── ui/
    ├── CampusApp.kt              Splash → setup/login → app
    ├── auth/                     Login, setup y estados de conexión
    ├── components/               Tarjetas, filtros, estados, formularios
    ├── model/                    Labels, formatos, opciones y fechas de formulario
    ├── navigation/               Destinos visibles según el rol
    ├── screens/
    │   ├── institutions/         Lista, formulario y eliminación
    │   ├── students/             Cuentas con rol participante
    │   ├── events/               Torneos y hackatones + formularios y apertura
    │   ├── records/              Disciplinas, participantes, equipos e inscripciones
    │   ├── profile/              Cuenta y conexión
    │   └── main/                 Drawer, top bar y bottom bar
    └── theme/                    Paleta y tipografía de la web
```

Capas:

- **DTO**: reflejan exactamente el JSON actual. Todos los campos tienen valores por defecto para tolerar campos ausentes o proyecciones reducidas (por ejemplo, catálogos).
- **Remoto**: Retrofit sobre los endpoints existentes; `ApiExecutor` convierte `{ "error": { ... } }` en `ApiException`.
- **Repositorio**: única puerta de entrada a la API para los ViewModels.
- **ViewModel + StateFlow**: estado inmutable, carga, refresco, búsqueda con debounce, paginación y acciones.
- **Compose**: pantallas sin lógica de red; `LazyColumn`, `Card`, `PullToRefreshBox`, `ModalBottomSheet`, estados de carga y error.

## Contrato de la API

Todas las respuestas exitosas usan el sobre:

```json
{ "data": ... }
```

Los errores usan:

```json
{ "error": { "code": "CODIGO", "message": "Texto", "fields": {} } }
```

Los listados paginados comparten:

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "pages": 1,
  "summary": { "total": 0, "active": 0, "cities": 0, "admins": 0 }
}
```

La `summary` varía según el recurso; por eso `SummaryDto` declara todos sus contadores como opcionales con valor 0.

### Endpoints utilizados

| Recurso | Endpoint | DTO |
| --- | --- | --- |
| Estado de instalación | `GET /api/setup/status` | `SetupStatusDto` |
| Crear administrador | `POST /api/setup/administrador` | `SessionDto` |
| Iniciar sesión | `POST /api/auth/login` | `SessionDto` |
| Sesión actual | `GET /api/auth/me` | `SessionDto` |
| Cerrar sesión | `POST /api/auth/logout` | `MessageDto` |
| Instituciones | `GET/POST/PUT/PATCH/DELETE /api/instituciones` | `InstitutionDto` |
| Cuentas | `GET/POST/PATCH /api/usuarios` | `UserDto` |
| Catálogos | `GET /api/catalogos` | `CatalogDto` |
| Disciplinas | `GET/POST/PATCH /api/disciplinas` | `DisciplineDto` |
| Torneos | `GET/POST/PATCH /api/torneos` | `TournamentDto` |
| Apertura de torneo | `PATCH /api/torneos/:id/estado` | `TournamentDto` |
| Hackatones | `GET/POST/PATCH /api/hackatones` | `HackathonDto` |
| Apertura de hackatón | `PATCH /api/hackatones/:id/estado` | `HackathonDto` |
| Participantes | `GET/POST/PATCH /api/participantes` | `ParticipantDto` |
| Equipos | `GET/POST/PATCH /api/equipos` | `TeamDto` |
| Inscripciones | `GET/POST /api/inscripciones` | `RegistrationDto` |

Los cuerpos de escritura viven en `AdminModels.kt` y replican **solo** los campos permitidos por el backend. Los valores nulos se omiten del JSON (`explicitNulls = false`), de modo que un equipo tecnológico no envía institución, disciplina, categoría ni modalidad, y uno deportivo no envía hackatón ni categoría tecnológica. Cualquier campo extra produciría `400 FORBIDDEN_FIELDS`.

Los campos enriquecidos por el backend (`disciplinaNombre`, `responsableNombre`, `inscritos`, `institucionNombre`, etc.) están declarados con su nombre exacto y se muestran tal como llegan.

### Autenticación

- El backend emite la cookie `campus_session` (`HttpOnly`).
- `SessionCookieJar` la conserva en memoria y en `SharedPreferences`, por lo que la sesión sobrevive al cierre de la app.
- El token CSRF se recibe en login/setup/me y se envía como `X-CSRF-Token` en POST, PUT, PATCH y DELETE.
- Un `401` limpia la sesión local y devuelve al login.
- Al abrir la app se consulta primero `/api/setup/status` y luego `/api/auth/me` para reutilizar la cookie existente.

### Regla de eliminación de instituciones

`DELETE /api/instituciones/:id` responde `409` con código `INSTITUTION_IN_USE` cuando la institución tiene registros asociados. La app muestra entonces la opción de **desactivar** mediante `PATCH /api/instituciones/:id` con `{ "estado": "inactivo" }`.

## Pantallas incluidas

- **Splash y conexión**: validación inicial de la API con la URL configurada, opción de reintentar y de cambiar de servidor.
- **Login / setup**: inicio de sesión o creación del primer administrador. Misma validación que la web (correo, contraseña de 10 caracteres, confirmación) y acceso al cambio de servidor.
- **Instituciones**: resumen, búsqueda, filtro por estado, paginación, refresco por gesto, alta/edición para administradores y eliminación con degradación a desactivación.
- **Estudiantes** (solo administrador): cuentas con rol `participante`; alta, edición, activación y desactivación.
- **Eventos**: pestañas de torneos y hackatones, búsqueda, filtros por estado, paginación, hoja de detalle y formularios de alta/edición. El administrador crea torneos y hackatones; el organizador crea torneos y edita los eventos que tiene asignados. El botón **Abrir inscripciones** ejecuta la transición `borrador → inscripciones` con confirmación.
- **Disciplinas**: alta y edición de modalidades, formatos y categorías (solo administrador).
- **Participantes**: alta y edición de deportistas vinculados a una cuenta `participante` y a una institución activa.
- **Equipos**: alta y edición de equipos deportivos o tecnológicos; la app filtra integrantes, responsables, instituciones, disciplinas, categorías y hackatones desde los catálogos.
- **Inscripciones**: alta de una inscripción en un torneo cuya ventana de inscripción esté abierta; el competidor puede ser un participante (modalidad individual) o un equipo deportivo compatible.
- **Mi perfil**: datos de la cuenta, rol, estado, URL de la API y cierre de sesión.

### Reglas de rol aplicadas en la app

| Acción | Administrador | Organizador | Participante |
| --- | --- | --- | --- |
| Crear cuentas de estudiante | Sí | No | No |
| Crear disciplinas | Sí | No | No |
| Crear torneos | Sí | Sí (responsable: su cuenta) | No |
| Crear hackatones | Sí | No | No |
| Editar eventos | Sí | Solo los asignados | No |
| Crear participantes | Sí | Sí | No |
| Crear equipos | Sí | Sí | Sí (responsable: su cuenta) |
| Crear inscripciones | Sí | Sí | Sí (solo sus participantes o equipos) |
| Abrir inscripciones | Sí | Sí, si es responsable | No |

La barra inferior del administrador incluye **Instituciones, Estudiantes, Eventos, Disciplinas y Mi perfil**. Los demás recursos se abren desde el menú lateral. Las tarjetas editables abren el formulario al tocarlas y el encabezado de cada listado ofrece **Nuevo** cuando el rol lo permite.

## Decisiones técnicas

- Sin Hilt: `AppContainer` construye Retrofit, repositorios y sesión una sola vez.
- Sin Navigation Compose: el alcance es una sola actividad con un menú lateral y una barra inferior; el estado de destino se conserva con `rememberSaveable`.
- Kotlinx Serialization en lugar de Gson para mantener nombres exactos y valores por defecto.
- Los DTO no se usan directamente como estado de UI: `EventUi.kt` y `Labels.kt` convierten fechas, estados y relaciones en textos legibles sin alterar los datos.
- `limit=10` en los listados para mantener fluida la lista y respetar el máximo de 50 de la API.

## Solución de problemas

| Síntoma | Causa probable |
| --- | --- |
| "No pudimos conectar con el servidor" con la URL por defecto | El servicio gratuito de Render está despertando; espera cerca de un minuto y reintenta. |
| "No pudimos conectar con el servidor" en el emulador local | El backend no está encendido o elegiste la URL equivocada; usa **Cambiar servidor → Emulador**. |
| Funciona en el emulador pero no en el teléfono con backend local | El backend escucha en `127.0.0.1` (`HOST=0.0.0.0` lo corrige), la IP es incorrecta o el firewall bloquea el puerto 3001. |
| La app vuelve al login | La cookie expiró o el backend se reinició; vuelve a iniciar sesión. |
| Error 403 al guardar | El token CSRF no está presente; ocurre si se intenta escribir sin una sesión obtenida por login/me. |
| No aparecen torneos en borrador | El backend solo muestra los borradores al usuario responsable; es el comportamiento existente. |
