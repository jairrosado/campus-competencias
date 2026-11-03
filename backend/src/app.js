/**
 * Archivo: backend/src/app.js
 * Propósito: API HTTP: setup inicial, autenticación, instituciones y montaje de los routers de usuarios y competencias.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - HU-04 · Registro y consulta de instituciones
 * - HU-05 · Edición, eliminación y desactivación de instituciones
 */
import express from 'express';
import mongoose from 'mongoose';
import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { User, Institution, publicUser, publicInstitution, states } from './models.js';
import { ApiError, credentials, institutionInput, escapedSearch, formFields } from './validation.js';
import { COOKIE, authLimiter, cookieOptions, createSession, hashPassword, verifyPassword, requireSession, requireAdmin, requireCsrf } from './security.js';
import { usersRouter } from './users.js';
import { competitionRouter } from './competition-routes.js';
import { Participant, Team, Registration } from './competition-models.js';
import { competitionWrite, refreshActor, requireRoles } from './competition-service.js';

// La web compilada con Vite queda en frontend/dist y se sirve desde la misma API.
// Así un único servicio (Render) atiende HTML, assets y /api sin problemas de cookies.
const frontendDist = fileURLToPath(new URL('../../frontend/dist', import.meta.url));
const frontendIndex = join(frontendDist, 'index.html');

function sameOrigin(req, origin) {
  if (!origin) return false;
  try {
    return new URL(origin).host === req.get('host');
  } catch {
    return false;
  }
}

const institutionFields = ['nombre', 'ciudad', 'departamento', 'tipo', 'estado'];
function institutionId(value) {
  if (!mongoose.isObjectIdOrHexString(value)) throw new ApiError(400, 'Identificador de institución inválido.');
  return value;
}
async function institutionRecord(value) {
  const record = await Institution.findById(institutionId(value));
  if (!record) throw new ApiError(404, 'Institución no encontrada.', 'NOT_FOUND');
  return record;
}
function institutionWrite(req, operation) {
  // Comparte la cola con las nuevas asociaciones para evitar referencias a registros eliminados.
  return competitionWrite(async () => {
    await refreshActor(req);
    requireRoles(req.user, ['administrador']);
    return operation();
  });
}

export function createApp(config) {
  const app = express();
  app.set('trust proxy', 1);
  app.disable('x-powered-by');
  app.use((req, res, next) => {
    res.set('X-Content-Type-Options', 'nosniff');
    res.set('Cache-Control', 'no-store');
    const origin = req.get('origin');
    // Se acepta el origen configurado y también el propio origen del servicio.
    // Esto último es clave cuando la API sirve la web compilada en el mismo dominio.
    const allowed = origin && (origin === config.clientOrigin || sameOrigin(req, origin));
    if (allowed) {
      res.set('Access-Control-Allow-Origin', origin);
      res.set('Access-Control-Allow-Credentials', 'true');
      res.set('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
      res.set('Access-Control-Allow-Headers', 'Content-Type, X-CSRF-Token');
      res.vary('Origin');
      if (req.method === 'OPTIONS') return res.sendStatus(204);
    }
    if (!['GET', 'HEAD', 'OPTIONS'].includes(req.method) && origin && !allowed) {
      throw new ApiError(403, 'El origen de la solicitud no está autorizado.', 'ORIGIN');
    }
    next();
  });
  app.use(express.json({ limit: '16kb' }));
  const authenticated = requireSession(config);
  const limiter = authLimiter();

  app.get('/api/health', (req, res) => {
    const connected = mongoose.connection.readyState === 1;
    res.status(connected ? 200 : 503).json({ data: { status: connected ? 'ok' : 'unavailable', database: connected } });
  });

  app.get('/api/setup/status', async (req, res) => {
    const exists = await User.exists({});
    res.json({ data: { needsSetup: !exists && config.allowInitialSetup, setupAllowed: config.allowInitialSetup } });
  });

  // HU-01 · Configuración inicial del administrador
  app.post('/api/setup/administrador', limiter, async (req, res) => {
    if (!config.allowInitialSetup) throw new ApiError(404, 'Operación no disponible.', 'NOT_FOUND');
    if (await User.exists({})) throw new ApiError(409, 'La cuenta inicial ya fue creada. Inicia sesión.', 'SETUP_CLOSED');
    const input = credentials(req.body, true);
    const user = await User.create({
      nombre: input.nombre, correo: input.correo,
      passwordHash: await hashPassword(input.password),
      rol: 'administrador', estado: 'activo', bootstrapAdmin: true,
    });
    res.status(201).json({ data: await createSession(res, user, config) });
  });

  // HU-01 · Inicio de sesión
  app.post('/api/auth/login', limiter, async (req, res) => {
    const input = credentials(req.body);
    const user = await User.findOne({ correo: input.correo }).select('+passwordHash +authVersion');
    const valid = await verifyPassword(input.password, user?.passwordHash);
    if (!valid || user?.estado !== 'activo') throw new ApiError(401, 'Correo o contraseña incorrectos.', 'INVALID_CREDENTIALS');
    res.json({ data: await createSession(res, user, config) });
  });

  // HU-01 · Sesión vigente y csrfToken
  app.get('/api/auth/me', authenticated, (req, res) => {
    res.json({ data: { user: publicUser(req.user), csrfToken: req.session.csrfToken } });
  });

  // HU-01 · Cierre de sesión
  app.post('/api/auth/logout', authenticated, requireCsrf, async (req, res) => {
    await req.session.deleteOne();
    res.clearCookie(COOKIE, cookieOptions(config));
    res.json({ data: { message: 'Sesión cerrada.' } });
  });

  app.use('/api/usuarios', usersRouter(authenticated));

  // HU-04 · Listado, búsqueda y resumen
  app.get('/api/instituciones', authenticated, async (req, res) => {
    const page = Number(req.query.page || 1);
    const limit = Number(req.query.limit || 10);
    if (!Number.isInteger(page) || page < 1 || page > 1000000 || !Number.isInteger(limit) || limit < 1 || limit > 50) {
      throw new ApiError(400, 'La paginación no es válida.');
    }
    const search = escapedSearch(req.query.q);
    const filter = {};
    if (search) filter.$or = ['nombre', 'ciudad', 'departamento'].map(field => ({ [field]: { $regex: search, $options: 'i' } }));
    if (req.query.estado) {
      if (!states.includes(req.query.estado)) throw new ApiError(400, 'Estado inválido.');
      filter.estado = req.query.estado;
    }
    const [items, total, all, active, cities] = await Promise.all([
      Institution.find(filter).sort({ nombre: 1, _id: 1 }).skip((page - 1) * limit).limit(limit).lean(),
      Institution.countDocuments(filter), Institution.countDocuments({}),
      Institution.countDocuments({ estado: 'activo' }), Institution.distinct('ciudad'),
    ]);
    res.json({ data: { items: items.map(publicInstitution), total, page, pages: Math.max(1, Math.ceil(total / limit)), summary: { total: all, active, cities: cities.length } } });
  });

  // HU-04 · Registro de instituciones
  app.post('/api/instituciones', authenticated, requireAdmin, requireCsrf, async (req, res) => {
    formFields(req.body, institutionFields);
    const institution = await institutionWrite(req, () => Institution.create(institutionInput(req.body)));
    res.status(201).json({ data: publicInstitution(institution) });
  });

  // HU-04 · Consulta de una institución
  app.get('/api/instituciones/:id', authenticated, async (req, res) => {
    res.json({ data: publicInstitution(await institutionRecord(req.params.id)) });
  });

  // HU-05 · Reemplazo completo
  app.put('/api/instituciones/:id', authenticated, requireAdmin, requireCsrf, async (req, res) => {
    formFields(req.body, institutionFields);
    if (institutionFields.some(field => !Object.hasOwn(req.body, field)) || !states.includes(req.body.estado)) {
      throw new ApiError(400, 'PUT requiere nombre, ciudad, departamento, tipo y estado válidos.');
    }
    const institution = await institutionWrite(req, async () => {
      const input = institutionInput(req.body);
      const updated = await Institution.findByIdAndUpdate(institutionId(req.params.id), input, { returnDocument: 'after', runValidators: true });
      if (!updated) throw new ApiError(404, 'Institución no encontrada.', 'NOT_FOUND');
      return updated;
    });
    res.json({ data: publicInstitution(institution) });
  });

  // HU-05 · Edición parcial
  app.patch('/api/instituciones/:id', authenticated, requireAdmin, requireCsrf, async (req, res) => {
    formFields(req.body, institutionFields);
    const institution = await institutionWrite(req, async () => {
      const record = await institutionRecord(req.params.id);
      Object.assign(record, institutionInput({ ...publicInstitution(record), ...req.body }));
      return record.save();
    });
    res.json({ data: publicInstitution(institution) });
  });

  // HU-05 · Borrado con protección de referencias
  app.delete('/api/instituciones/:id', authenticated, requireAdmin, requireCsrf, async (req, res) => {
    const deleted = await institutionWrite(req, async () => {
      const institution = await institutionRecord(req.params.id);
      const references = await Promise.all([
        Participant.exists({ institucion: institution._id }),
        Team.exists({ institucion: institution._id }),
        Registration.exists({ $or: [{ institucion: institution._id }, { 'plantilla.institucion': institution._id }] }),
      ]);
      if (references.some(Boolean)) throw new ApiError(409, 'La institución tiene participantes, equipos o inscripciones. Desactívala para conservar su historial.', 'INSTITUTION_IN_USE');
      const deleted = await Institution.findByIdAndDelete(institution._id);
      if (!deleted) throw new ApiError(404, 'Institución no encontrada.', 'NOT_FOUND');
      return deleted;
    });
    res.json({ data: { id: String(deleted._id), message: 'Institución eliminada.' } });
  });

  app.use('/api', competitionRouter(authenticated));

  // HU-01 · Web compilada en el mismo servicio (producción).
  if (existsSync(frontendIndex)) {
    app.use(express.static(frontendDist, { index: false }));
    app.use((req, res, next) => {
      if (!['GET', 'HEAD'].includes(req.method) || req.path.startsWith('/api')) return next();
      res.sendFile(frontendIndex);
    });
  } else {
    app.get('/', (req, res) => res.json({
      data: { status: 'ok', message: 'API en línea. La web no está compilada (falta frontend/dist).' },
    }));
  }

  app.use((req, res) => res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Recurso no encontrado.' } }));
  app.use((error, req, res, next) => {
    if (res.headersSent) return next(error);
    if (error.code === 11000) {
      let message = 'Ya existe una institución con ese nombre y ubicación.';
      if (req.path.startsWith('/api/setup')) message = 'La cuenta inicial ya existe. Inicia sesión.';
      else if (req.path.startsWith('/api/usuarios')) message = 'Ya existe una cuenta con ese correo.';
      else if (req.path.startsWith('/api/disciplinas')) message = 'Ya existe una disciplina con ese nombre.';
      else if (req.path.startsWith('/api/participantes')) message = 'Ya existe un participante con ese documento o cuenta.';
      else if (req.path.startsWith('/api/equipos')) message = 'Ya existe un equipo con ese nombre en esta categoría.';
      else if (req.path.startsWith('/api/inscripciones')) message = 'Este competidor ya está inscrito en la prueba.';
      return res.status(409).json({ error: { code: 'DUPLICATE', message } });
    }
    if (error.type === 'entity.parse.failed') return res.status(400).json({ error: { code: 'INVALID_JSON', message: 'El formato enviado no es válido.' } });
    if (error.type === 'entity.too.large') return res.status(413).json({ error: { code: 'TOO_LARGE', message: 'La solicitud es demasiado grande.' } });
    if (error.name === 'ValidationError' || error.name === 'CastError') {
      return res.status(400).json({ error: { code: 'VALIDATION', message: 'Revisa los datos enviados.' } });
    }
    const status = error.status || 500;
    if (status >= 500) console.error('La API no pudo completar una operación:', error.name);
    res.status(status).json({ error: { code: error.code || 'INTERNAL', message: status < 500 ? error.message : 'No se pudo completar la operación. Intenta nuevamente.', ...(error.fields ? { fields: error.fields } : {}) } });
  });
  return app;
}
