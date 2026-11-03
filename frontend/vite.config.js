/**
 * Archivo: frontend/vite.config.js
 * Propósito: Configuración del servidor de desarrollo web.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 */
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  // Carga VITE_API_URL desde frontend/.env y conserva la posibilidad de usar
  // variables definidas en el proceso (CI o terminal).
  const env = { ...loadEnv(mode, process.cwd(), ''), ...process.env };
  const webPort = Number(env.CAMPUS_WEB_PORT || 5173);
  const apiPort = Number(env.CAMPUS_API_PORT || 3001);
  if (!Number.isInteger(webPort) || webPort < 1 || webPort > 65535) {
    throw new Error('El puerto web de Campus debe estar entre 1 y 65535.');
  }
  if (!Number.isInteger(apiPort) || apiPort < 1 || apiPort > 65535) {
    throw new Error('El puerto API de Campus debe estar entre 1 y 65535.');
  }
  const proxyTarget = env.VITE_API_URL?.trim().replace(/\/+$/, '')
    || env.CAMPUS_API_URL
    || `http://localhost:${apiPort}`;

  return {
    plugins: [react()],
    server: {
      host: '127.0.0.1', port: webPort, strictPort: true,
      proxy: { '/api': proxyTarget },
    },
  };
});
