/**
 * Archivo: backend/src/config.js
 * Propósito: Configuración por entorno: puerto, host accesible desde emulador/celular, MongoDB y sesión.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
import dotenv from 'dotenv';
import { fileURLToPath } from 'node:url';

dotenv.config({ path: fileURLToPath(new URL('../.env', import.meta.url)), quiet: true });

export function readConfig(env = process.env) {
  const port = Number(env.PORT || 3001);
  const sessionHours = Number(env.SESSION_HOURS || 8);
  if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('PORT inválido.');
  if (!Number.isFinite(sessionHours) || sessionHours <= 0 || sessionHours > 72) {
    throw new Error('SESSION_HOURS debe estar entre 0 y 72 horas.');
  }
  const production = env.NODE_ENV === 'production';
  return {
    port,
    // 0.0.0.0 permite conexiones del emulador y de celulares en la misma red.
    // Usa 127.0.0.1 solo si quieres aislar la API en este equipo.
    host: env.HOST || '0.0.0.0',
    mongoUri: env.MONGODB_URI || 'mongodb://127.0.0.1:27017/torneos_universidad',
    clientOrigin: new URL(env.CLIENT_ORIGIN || 'http://127.0.0.1:5173').origin,
    sessionHours,
    production,
    allowInitialSetup: env.ALLOW_INITIAL_SETUP === 'true',
  };
}
