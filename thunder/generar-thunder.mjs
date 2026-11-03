/**
 * Archivo: thunder/generar-thunder.mjs
 * Propósito: Genera la colección de Thunder Client (formato Postman 2.1) y el entorno local.
 *
 * Historias de usuario:
 * - HU-01 a HU-13 · Todos los endpoints de la API usados por las historias
 */
// Uso: node thunder/generar-thunder.mjs
import { readFile, writeFile } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const collectionPath = join(here, 'campus-competencias.postman_collection.json');
const environmentPath = join(here, 'campus-competencias.postman_environment.json');
const envFilePath = join(here, 'campus-competencias.env');

const API = '{{baseUrl}}';

function request({ name, method = 'GET', path, body, csrf = false, description = '', tests = [] }) {
  const header = [];
  if (body !== undefined) header.push({ key: 'Content-Type', value: 'application/json', type: 'text' });
  if (csrf) header.push({ key: 'X-CSRF-Token', value: '{{csrfToken}}', type: 'text' });

  const item = {
    name,
    request: { method, header, url: `${API}${path}`, description },
  };
  if (body !== undefined) {
    item.request.body = {
      mode: 'raw',
      raw: JSON.stringify(body, null, 2),
      options: { raw: { language: 'json' } },
    };
  }
  if (tests.length) {
    item.event = [{ listen: 'test', script: { type: 'text/javascript', exec: tests } }];
  }
  return item;
}

const saveSession = [
  'const data = tc.response.json && tc.response.json.data;',
  'if (data && data.csrfToken) tc.setVar("csrfToken", data.csrfToken);',
  'if (data && data.user && data.user.id) tc.setVar("usuarioId", data.user.id);',
];

const saveCsrf = [
  'const data = tc.response.json && tc.response.json.data;',
  'if (data && data.csrfToken) tc.setVar("csrfToken", data.csrfToken);',
];

const saveId = (variable) => [
  'const data = tc.response.json && tc.response.json.data;',
  `if (data && data.id) tc.setVar("${variable}", data.id);`,
];

const saveDiscipline = [
  'const data = tc.response.json && tc.response.json.data;',
  'if (data && data.id) tc.setVar("disciplinaId", data.id);',
  'if (data && data.categorias && data.categorias[0] && data.categorias[0].id) tc.setVar("categoriaId", data.categorias[0].id);',
];

const clearSession = [
  'tc.setVar("csrfToken", "");',
  'tc.setVar("usuarioId", "");',
];

const folder = (name, description, item) => ({ name, description, item });

const collection = {
  info: {
    _postman_id: randomUUID(),
    name: 'Campus Competencias · API',
    description: [
      'Colección completa de la API de Campus Competencias para Thunder Client / Postman.',
      '',
      'Flujo mínimo:',
      '1. Importa el entorno `campus-competencias.env` y actívalo.',
      '2. Ajusta `baseUrl`, `correo` y `password`.',
      '3. Ejecuta `POST Login` y copia `data.csrfToken` en la variable `csrfToken` si tu versión no ejecuta scripts.',
      '4. Todas las escrituras (POST/PUT/PATCH/DELETE) envían `X-CSRF-Token: {{csrfToken}}`.',
      '',
      'La cookie `campus_session` es HttpOnly: Thunder Client la guarda automáticamente.',
      'Consulta `thunder/README.md` para el detalle de roles, variables y errores.',
    ].join('\n'),
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
  },
  item: [
    folder('00 · Salud y setup', 'Endpoints públicos. Sirven para saber si el servidor y MongoDB están arriba y si falta crear la cuenta inicial.', [
      request({
        name: 'Health · estado del servidor',
        path: '/api/health',
        description: 'Público. 200 si MongoDB está conectado; 503 si no.',
      }),
      request({
        name: 'Setup · estado inicial',
        path: '/api/setup/status',
        description: 'Público. `needsSetup: true` cuando no existe ningún usuario y el setup está habilitado.',
      }),
      request({
        name: 'Setup · crear administrador',
        method: 'POST',
        path: '/api/setup/administrador',
        description: 'Público, solo si `needsSetup` es true. Devuelve sesión y csrfToken. No requiere CSRF.\n\n409 SETUP_CLOSED si ya existe una cuenta.',
        body: {
          nombre: 'Administrador Campus',
          correo: '{{correo}}',
          password: '{{password}}',
        },
        tests: saveSession,
      }),
    ]),

    folder('01 · Autenticación', 'Cookie de sesión + CSRF. Ejecuta Login antes de cualquier otro endpoint protegido.', [
      request({
        name: 'Login',
        method: 'POST',
        path: '/api/auth/login',
        description: 'Público. Guarda la cookie `campus_session` y devuelve `data.csrfToken`.\n\nMáximo 10 intentos por minuto y por IP (429 RATE_LIMIT).',
        body: { correo: '{{correo}}', password: '{{password}}' },
        tests: saveSession,
      }),
      request({
        name: 'Me · sesión actual',
        path: '/api/auth/me',
        description: 'Protegido. Devuelve el usuario autenticado y un csrfToken vigente. Útil para renovar `csrfToken` sin volver a iniciar sesión.',
        tests: saveCsrf,
      }),
      request({
        name: 'Logout',
        method: 'POST',
        path: '/api/auth/logout',
        csrf: true,
        description: 'Protegido + CSRF. Elimina la sesión y limpia la cookie.',
        tests: clearSession,
      }),
    ]),

    folder('02 · Usuarios', 'Solo administrador (excepto `PATCH /me`). Roles: administrador, organizador, participante, jurado.', [
      request({
        name: 'Usuarios · listar',
        path: '/api/usuarios?page=1&limit=10',
        description: 'Admin. Filtros opcionales: `q`, `rol`, `estado`. Paginación: page >= 1, limit 1..50.',
      }),
      request({
        name: 'Usuarios · crear organizador',
        method: 'POST',
        path: '/api/usuarios',
        csrf: true,
        description: 'Admin + CSRF. La contraseña debe tener 10–128 caracteres. Guarda el id en `organizadorId`.\n\n409 DUPLICATE si el correo ya existe.',
        body: {
          nombre: 'Organizador Demo',
          correo: 'organizador.demo@campus.edu.co',
          rol: 'organizador',
          estado: 'activo',
          password: 'Organizador123',
        },
        tests: saveId('organizadorId'),
      }),
      request({
        name: 'Usuarios · crear participante',
        method: 'POST',
        path: '/api/usuarios',
        csrf: true,
        description: 'Admin + CSRF. Crea la cuenta que se asociará a un participante. Guarda el id en `participanteUsuarioId`.',
        body: {
          nombre: 'Laura Gómez',
          correo: 'laura.demo@campus.edu.co',
          rol: 'participante',
          estado: 'activo',
          password: 'Participante123',
        },
        tests: saveId('participanteUsuarioId'),
      }),
      request({
        name: 'Usuarios · actualizar mi perfil',
        method: 'PATCH',
        path: '/api/usuarios/me',
        csrf: true,
        description: 'Usuario autenticado + CSRF. Para cambiar el correo debes enviar también `currentPassword`.',
        body: { nombre: 'Administrador Campus' },
      }),
      request({
        name: 'Usuarios · consultar por id',
        path: '/api/usuarios/{{usuarioId}}',
        description: 'Admin o el propio usuario. 403 FORBIDDEN si consultas otro perfil sin ser admin.',
      }),
      request({
        name: 'Usuarios · actualizar por id',
        method: 'PATCH',
        path: '/api/usuarios/{{organizadorId}}',
        csrf: true,
        description: 'Admin + CSRF. Los campos ausentes conservan su valor. `password` es opcional; al cambiarla se invalidan las sesiones del usuario.\n\nNo puedes desactivar tu propia cuenta ni dejar la app sin administradores activos.',
        body: { estado: 'activo' },
      }),
    ]),

    folder('03 · Instituciones', 'Lectura para cualquier sesión; escritura solo administrador + CSRF.', [
      request({
        name: 'Instituciones · listar',
        path: '/api/instituciones?page=1&limit=10',
        description: 'Protegido. Filtros: `q`, `estado` (activo/inactivo). Incluye `summary` con total, activos y ciudades.',
      }),
      request({
        name: 'Instituciones · crear',
        method: 'POST',
        path: '/api/instituciones',
        csrf: true,
        description: 'Admin + CSRF. Tipos: colegio, universidad, instituto, otro. Guarda el id en `institucionId`.\n\n409 DUPLICATE si nombre + ciudad + departamento ya existen.',
        body: {
          nombre: 'Universidad Campus',
          ciudad: 'Bogotá',
          departamento: 'Cundinamarca',
          tipo: 'universidad',
          estado: 'activo',
        },
        tests: saveId('institucionId'),
      }),
      request({
        name: 'Instituciones · consultar por id',
        path: '/api/instituciones/{{institucionId}}',
        description: 'Protegido. 404 NOT_FOUND si el id no existe.',
      }),
      request({
        name: 'Instituciones · reemplazar (PUT)',
        method: 'PUT',
        path: '/api/instituciones/{{institucionId}}',
        csrf: true,
        description: 'Admin + CSRF. PUT exige los cinco campos: nombre, ciudad, departamento, tipo y estado.',
        body: {
          nombre: 'Universidad Campus',
          ciudad: 'Bogotá D.C.',
          departamento: 'Cundinamarca',
          tipo: 'universidad',
          estado: 'activo',
        },
      }),
      request({
        name: 'Instituciones · actualizar (PATCH)',
        method: 'PATCH',
        path: '/api/instituciones/{{institucionId}}',
        csrf: true,
        description: 'Admin + CSRF. Actualización parcial: solo envía los campos que cambian.',
        body: { ciudad: 'Bogotá' },
      }),
      request({
        name: 'Instituciones · eliminar',
        method: 'DELETE',
        path: '/api/instituciones/{{institucionId}}',
        csrf: true,
        description: 'Admin + CSRF. Si tiene participantes, equipos o inscripciones responde 409 INSTITUTION_IN_USE: desactívala en lugar de borrarla.',
      }),
    ]),

    folder('04 · Catálogos', 'Una sola llamada con instituciones, disciplinas, usuarios, participantes, equipos, hackatones y torneos visibles para el rol.', [
      request({
        name: 'Catálogos · todo',
        path: '/api/catalogos',
        description: 'Protegido. Para jurado, participantes y equipos vienen vacíos. Para organizador/participante se limita al alcance de sus registros.',
      }),
    ]),

    folder('05 · Disciplinas', 'Crear y editar solo administrador. La categoría creada aquí se usa en torneos y equipos.', [
      request({
        name: 'Disciplinas · listar',
        path: '/api/disciplinas?page=1&limit=10',
        description: 'Protegido. Filtros: `q`, `estado`.',
      }),
      request({
        name: 'Disciplinas · consultar por id',
        path: '/api/disciplinas/{{disciplinaId}}',
        description: 'Protegido. Devuelve modalidades, formatos y categorías con su id.',
      }),
      request({
        name: 'Disciplinas · crear',
        method: 'POST',
        path: '/api/disciplinas',
        csrf: true,
        description: 'Admin + CSRF. Modalidades: equipo, individual, relevo. Formatos: liga, grupos, eliminacion, pruebas, rondas.\n\nGuarda `disciplinaId` y el id de la primera categoría en `categoriaId`.',
        body: {
          nombre: 'Baloncesto',
          modalidades: ['individual', 'equipo'],
          formatos: ['liga', 'eliminacion', 'grupos'],
          categorias: [
            { nombre: 'Abierta', edadMin: null, edadMax: null, genero: 'cualquiera' },
          ],
          estado: 'activo',
        },
        tests: saveDiscipline,
      }),
      request({
        name: 'Disciplinas · actualizar',
        method: 'PATCH',
        path: '/api/disciplinas/{{disciplinaId}}',
        csrf: true,
        description: 'Admin + CSRF. No puedes retirar modalidades, formatos ni categorías que ya estén en uso (409).',
        body: { estado: 'activo' },
      }),
    ]),

    folder('06 · Torneos', 'Crear/editar: administrador u organizador responsable. Abrir inscripciones solo desde borrador.', [
      request({
        name: 'Torneos · listar',
        path: '/api/torneos?page=1&limit=10',
        description: 'Protegido. Filtros: `q`, `estado`, `tipo`, `disciplina`, `institucion`, `responsable`, `categoria`.\n\nEstados: borrador, inscripciones, en_curso, finalizado, cancelado.',
      }),
      request({
        name: 'Torneos · consultar por id',
        path: '/api/torneos/{{torneoId}}',
        description: 'Protegido. Un borrador solo lo ve su responsable o un administrador. Incluye `inscritos`.',
      }),
      request({
        name: 'Torneos · crear',
        method: 'POST',
        path: '/api/torneos',
        csrf: true,
        description: 'Admin/Organizador + CSRF. Fechas ISO 8601 con zona horaria. Deben cumplir: inscripcionInicio <= inscripcionFin <= inicio <= fin.\n\n`categoria` es el id de una categoría de la disciplina. `unidadInscripcion` y `formato` deben existir en la disciplina. Guarda el id en `torneoId`.',
        body: {
          nombre: 'Torneo Apertura 2026',
          descripcion: 'Torneo de prueba para validar la API.',
          tipo: 'universitario',
          disciplina: '{{disciplinaId}}',
          categoria: '{{categoriaId}}',
          unidadInscripcion: 'individual',
          formato: 'liga',
          ciudad: 'Bogotá',
          departamento: 'Cundinamarca',
          inicio: '2027-01-15T08:00:00.000-05:00',
          fin: '2027-02-15T18:00:00.000-05:00',
          inscripcionInicio: '2026-10-01T08:00:00.000-05:00',
          inscripcionFin: '2026-12-15T18:00:00.000-05:00',
          cupos: 32,
          responsable: '{{usuarioId}}',
          reglamento: {
            integrantesMin: 1,
            integrantesMax: 1,
            maxInscripciones: 2,
            metrica: 'puntos',
            unidad: 'puntos',
            precision: 0,
            orden: 'descendente',
            fases: ['Fase regular', 'Semifinal', 'Final'],
            avance: 'Clasifican los dos primeros de cada grupo.',
            desempates: ['Diferencia de puntos', 'Sorteo'],
            pruebas: [],
            requisitos: 'Documento de identidad vigente.',
            permiteMultiplesEquipos: false,
          },
        },
        tests: saveId('torneoId'),
      }),
      request({
        name: 'Torneos · actualizar',
        method: 'PATCH',
        path: '/api/torneos/{{torneoId}}',
        csrf: true,
        description: 'Admin/Organizador responsable + CSRF. En borrador se puede editar todo. Con inscripciones abiertas el reglamento y el formato quedan bloqueados (409 LOCKED_RULES).',
        body: { cupos: 40 },
      }),
      request({
        name: 'Torneos · abrir inscripciones',
        method: 'PATCH',
        path: '/api/torneos/{{torneoId}}/estado',
        csrf: true,
        description: 'Admin/Organizador responsable + CSRF. Esta entrega solo permite borrador -> inscripciones.\n\nRequiere que la disciplina esté activa. Necesario antes de `POST Inscripciones`.',
        body: { estado: 'inscripciones' },
      }),
    ]),

    folder('07 · Hackatones', 'Crear y editar solo administrador u organizador responsable.', [
      request({
        name: 'Hackatones · listar',
        path: '/api/hackatones?page=1&limit=10',
        description: 'Protegido. Filtros: `q`, `estado`, `categoria`, `responsable`.\n\nEstados: borrador, inscripciones, entrega, evaluacion, publicado, cancelado.',
      }),
      request({
        name: 'Hackatones · consultar por id',
        path: '/api/hackatones/{{hackatonId}}',
        description: 'Protegido. Un borrador solo lo ve su responsable o un administrador.',
      }),
      request({
        name: 'Hackatones · crear',
        method: 'POST',
        path: '/api/hackatones',
        csrf: true,
        description: 'Admin/Organizador + CSRF. Fechas ISO 8601. Orden obligatorio: inscripcionFin <= inicio <= entregaInicio <= entregaFin <= evaluacionInicio <= evaluacionFin <= fin. Guarda el id en `hackatonId`.',
        body: {
          nombre: 'Hackatón Campus 2027',
          descripcion: 'Reto de innovación para estudiantes.',
          ciudad: 'Medellín',
          departamento: 'Antioquia',
          modalidad: 'presencial',
          inicio: '2027-05-01T08:00:00.000-05:00',
          fin: '2027-05-20T18:00:00.000-05:00',
          inscripcionInicio: '2027-04-01T08:00:00.000-05:00',
          inscripcionFin: '2027-04-15T18:00:00.000-05:00',
          entregaInicio: '2027-05-02T08:00:00.000-05:00',
          entregaFin: '2027-05-09T18:00:00.000-05:00',
          evaluacionInicio: '2027-05-10T08:00:00.000-05:00',
          evaluacionFin: '2027-05-15T18:00:00.000-05:00',
          cupos: 20,
          integrantesMin: 1,
          integrantesMax: 4,
          proyectosPorEquipo: 1,
          categorias: ['Salud', 'Educación'],
          requisitos: 'Equipo conformado y propuesta inicial.',
          responsable: '{{usuarioId}}',
        },
        tests: saveId('hackatonId'),
      }),
      request({
        name: 'Hackatones · actualizar',
        method: 'PATCH',
        path: '/api/hackatones/{{hackatonId}}',
        csrf: true,
        description: 'Admin/Organizador responsable + CSRF. Con el evento cerrado (publicado/cancelado/finalizado) responde 409.',
        body: { cupos: 25 },
      }),
      request({
        name: 'Hackatones · abrir inscripciones',
        method: 'PATCH',
        path: '/api/hackatones/{{hackatonId}}/estado',
        csrf: true,
        description: 'Admin/Organizador responsable + CSRF. Solo permite borrador -> inscripciones.',
        body: { estado: 'inscripciones' },
      }),
    ]),

    folder('08 · Participantes', 'Administrador u organizador. La cuenta asociada debe tener rol participante.', [
      request({
        name: 'Participantes · listar',
        path: '/api/participantes?page=1&limit=10',
        description: 'Protegido. Un organizador ve los que creó; un participante solo los suyos.',
      }),
      request({
        name: 'Participantes · consultar por id',
        path: '/api/participantes/{{participanteId}}',
        description: 'Protegido. 404 si está fuera de tu alcance.',
      }),
      request({
        name: 'Participantes · crear',
        method: 'POST',
        path: '/api/participantes',
        csrf: true,
        description: 'Admin/Organizador + CSRF. `nacimiento` en formato YYYY-MM-DD. `usuario` debe ser una cuenta activa con rol participante. Guarda el id en `participanteId`.',
        body: {
          documento: '1002003004',
          nombre: 'Laura Gómez',
          nacimiento: '2004-05-12',
          genero: 'femenino',
          usuario: '{{participanteUsuarioId}}',
          institucion: '{{institucionId}}',
          categoria: 'Abierta',
          estado: 'activo',
        },
        tests: saveId('participanteId'),
      }),
      request({
        name: 'Participantes · actualizar',
        method: 'PATCH',
        path: '/api/participantes/{{participanteId}}',
        csrf: true,
        description: 'Admin/Organizador + CSRF. Actualización parcial; los campos ausentes se conservan.',
        body: { estado: 'activo' },
      }),
    ]),

    folder('09 · Equipos', 'Deportivos: institución + disciplina + categoría + modalidad. Tecnológicos: hackatón + categoría tecnológica.', [
      request({
        name: 'Equipos · listar',
        path: '/api/equipos?page=1&limit=10',
        description: 'Protegido. Filtros: `q`, `estado`, `tipo`, `disciplina`, `institucion`, `responsable`, `categoria`.',
      }),
      request({
        name: 'Equipos · consultar por id',
        path: '/api/equipos/{{equipoId}}',
        description: 'Protegido. 404 si está fuera de tu alcance.',
      }),
      request({
        name: 'Equipos · crear deportivo',
        method: 'POST',
        path: '/api/equipos',
        csrf: true,
        description: 'Admin/Organizador/Participante + CSRF. Todos los integrantes deben estar activos, pertenecer a la institución y cumplir edad/género de la categoría. `modalidad` no puede ser individual. Guarda el id en `equipoId`.',
        body: {
          nombre: 'Águilas Campus',
          tipo: 'deportivo',
          responsable: '{{usuarioId}}',
          estado: 'activo',
          institucion: '{{institucionId}}',
          disciplina: '{{disciplinaId}}',
          categoria: '{{categoriaId}}',
          modalidad: 'equipo',
          integrantes: ['{{participanteId}}'],
        },
        tests: saveId('equipoId'),
      }),
      request({
        name: 'Equipos · crear tecnológico',
        method: 'POST',
        path: '/api/equipos',
        csrf: true,
        description: 'Admin/Organizador/Participante + CSRF. No envíes institución, disciplina, categoría ni modalidad. La plantilla debe cumplir integrantesMin/Max del hackatón. Guarda el id en `equipoTecnologicoId`.',
        body: {
          nombre: 'Equipo Innovación',
          tipo: 'tecnologico',
          responsable: '{{usuarioId}}',
          estado: 'activo',
          hackaton: '{{hackatonId}}',
          categoriaTecnologica: 'Salud',
          integrantes: ['{{participanteId}}'],
        },
        tests: saveId('equipoTecnologicoId'),
      }),
      request({
        name: 'Equipos · actualizar',
        method: 'PATCH',
        path: '/api/equipos/{{equipoId}}',
        csrf: true,
        description: 'Admin/Organizador/Participante responsable + CSRF. El tipo no se puede cambiar. Si el equipo ya tiene inscripciones, la plantilla y las referencias quedan bloqueadas (409 LOCKED_ROSTER).',
        body: { estado: 'activo' },
      }),
    ]),

    folder('10 · Inscripciones', 'El torneo debe estar en estado `inscripciones` y dentro de la ventana de fechas.', [
      request({
        name: 'Inscripciones · listar',
        path: '/api/inscripciones?page=1&limit=10',
        description: 'Protegido. Filtro opcional: `torneo={{torneoId}}`. Organizador ve sus torneos; participante ve sus competidores.',
      }),
      request({
        name: 'Inscripciones · crear individual',
        method: 'POST',
        path: '/api/inscripciones',
        csrf: true,
        description: 'Admin/Organizador/Participante + CSRF. `modalidad` debe ser la unidad de inscripción del torneo. Si el reglamento tiene pruebas, envía una de ellas; si no, deja `prueba` vacío.\n\nPara equipo: cambia `modalidad` a "equipo" y `competidor` a `{{equipoId}}` (mismo torneo, disciplina, categoría y modalidad).',
        body: {
          torneo: '{{torneoId}}',
          modalidad: 'individual',
          competidor: '{{participanteId}}',
          prueba: '',
        },
      }),
    ]),
  ],
};

const variables = [
  ['baseUrl', 'http://localhost:3001', 'Servidor local. Emulador Android: http://10.0.2.2:3001. Celular: IP LAN del PC.'],
  ['correo', 'admin@campus.edu.co', 'Correo de una cuenta existente.'],
  ['password', 'Campus2026*', 'Contraseña de esa cuenta (10–128 caracteres).'],
  ['csrfToken', '', 'Se llena con la respuesta de Login/Me o manualmente.'],
  ['usuarioId', '000000000000000000000000', 'Id del usuario autenticado; Login lo guarda automáticamente.'],
  ['organizadorId', '000000000000000000000000', 'Id de un organizador (lo guarda "Usuarios · crear organizador").'],
  ['participanteUsuarioId', '000000000000000000000000', 'Cuenta con rol participante (la guarda "Usuarios · crear participante").'],
  ['institucionId', '000000000000000000000000', 'Id de una institución.'],
  ['disciplinaId', '000000000000000000000000', 'Id de una disciplina.'],
  ['categoriaId', '000000000000000000000000', 'Id de una categoría de esa disciplina.'],
  ['torneoId', '000000000000000000000000', 'Id de un torneo en estado inscripciones.'],
  ['hackatonId', '000000000000000000000000', 'Id de un hackatón.'],
  ['participanteId', '000000000000000000000000', 'Id de un participante activo.'],
  ['equipoId', '000000000000000000000000', 'Id de un equipo deportivo.'],
  ['equipoTecnologicoId', '000000000000000000000000', 'Id de un equipo tecnológico.'],
];

const environment = {
  id: randomUUID(),
  name: 'Campus Competencias · Local',
  values: variables.map(([key, value]) => ({ key, value, type: 'default', enabled: true })),
  _postman_variable_scope: 'environment',
  _postman_exported_at: new Date().toISOString(),
  _postman_exported_using: 'Thunder Client / Campus Competencias',
};

const envFile = variables.map(([key, value]) => `${key}=${value}`).join('\n') + '\n';

await writeFile(collectionPath, JSON.stringify(collection, null, 2) + '\n', 'utf8');
await writeFile(environmentPath, JSON.stringify(environment, null, 2) + '\n', 'utf8');
await writeFile(envFilePath, envFile, 'utf8');

const readBack = JSON.parse(await readFile(collectionPath, 'utf8'));
const count = (items) => items.reduce((total, item) => total + (item.item ? count(item.item) : 1), 0);
JSON.parse(await readFile(environmentPath, 'utf8'));

console.log(`Colección: ${count(readBack.item)} solicitudes en ${readBack.item.length} carpetas.`);
console.log(`Variables: ${variables.length}.`);
console.log('Archivos generados:');
console.log(`- ${collectionPath}`);
console.log(`- ${environmentPath}`);
console.log(`- ${envFilePath}`);
