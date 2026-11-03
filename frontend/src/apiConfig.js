/**
 * Configuración de red del cliente web.
 * En desarrollo, el proxy de Vite envía /api a localhost:3001. VITE_API_URL
 * permite apuntar el cliente directamente a otra máquina o entorno.
 */
const configuredBaseUrl = import.meta.env.VITE_API_URL?.trim().replace(/\/+$/, '');

export const API_BASE_URL = configuredBaseUrl || '';
export const API_PREFIX = API_BASE_URL ? `${API_BASE_URL}/api` : '/api';
export const API_CREDENTIALS = API_BASE_URL ? 'include' : 'same-origin';

export function apiUrl(path) {
  return `${API_PREFIX}${path.startsWith('/') ? path : `/${path}`}`;
}
