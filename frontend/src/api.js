/**
 * Archivo: frontend/src/api.js
 * Propósito: Cliente HTTP de la web: sobre data/error, cookie de sesión y header X-CSRF-Token.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 */
import { apiUrl, API_CREDENTIALS } from './apiConfig.js';

let csrfToken = null;

export function setCsrf(value) { csrfToken = value; }

// HU-01 · Cliente HTTP con cookie y CSRF
export async function api(path, options = {}) {
  const { body, method = 'GET', signal } = options;
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  // HU-01 · Header CSRF en escrituras
  if (csrfToken && method !== 'GET') headers['X-CSRF-Token'] = csrfToken;
  let response;
  try {
    response = await fetch(apiUrl(path), {
      method, headers, credentials: API_CREDENTIALS,
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}), signal,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new Error('No pudimos conectar con el servidor. Intenta nuevamente.');
  }
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(payload.error?.message || 'No se pudo completar la solicitud.');
    error.status = response.status;
    error.code = payload.error?.code;
    throw error;
  }
  return payload.data;
}
