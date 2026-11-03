/**
 * Archivo: backend/src/server.js
 * Propósito: Arranque del servidor, conexión a MongoDB y anuncio de las URLs local, LAN y emulador.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 * - AN-01 · Inicio de sesión y configuración inicial en Android
 */
import mongoose from 'mongoose';
import { networkInterfaces } from 'node:os';
import { readConfig } from './config.js';
import { initializeModels } from './models.js';
import { createApp } from './app.js';

const config = readConfig();
let server;
let stopping = false;

function lanUrls(port) {
  const addresses = [];
  for (const entries of Object.values(networkInterfaces())) {
    for (const entry of entries || []) {
      if (entry.family === 'IPv4' && !entry.internal) addresses.push(`http://${entry.address}:${port}`);
    }
  }
  return [...new Set(addresses)];
}

async function stop() {
  if (stopping) return;
  stopping = true;
  if (server) await new Promise(resolve => server.close(resolve));
  await mongoose.disconnect();
  process.exit(0);
}

process.on('SIGINT', stop);
process.on('SIGTERM', stop);

try {
  await mongoose.connect(config.mongoUri, { serverSelectionTimeoutMS: 5000 });
  await initializeModels();
  server = createApp(config).listen(config.port, config.host, () => {
    console.log(`API escuchando en ${config.host}:${config.port}`);
    if (config.host === '127.0.0.1' || config.host === 'localhost') {
      console.warn('HOST solo acepta conexiones locales. Usa HOST=0.0.0.0 para el celular.');
    }
    console.log(`  Equipo:            http://localhost:${config.port}`);
    const lan = lanUrls(config.port);
    if (lan.length) {
      console.log(`  Celular (Wi-Fi):   ${lan.join('  o  ')}`);
    } else {
      console.log('  Celular (Wi-Fi):   conéctate a una red y usa la IP local de este equipo.');
    }
    console.log(`  Emulador Android:  http://10.0.2.2:${config.port}`);
    console.log('MongoDB conectado. La base se crea al guardar el primer registro.');
  });
  server.on('error', async () => {
    console.error(`No se pudo abrir el puerto ${config.port}. Revisa si ya hay otro servidor utilizándolo.`);
    await mongoose.disconnect();
    process.exit(1);
  });
} catch {
  console.error('No se pudo conectar con MongoDB. Comprueba que el servidor esté iniciado y revisa MONGODB_URI en backend/.env.');
  await mongoose.disconnect();
  process.exit(1);
}
