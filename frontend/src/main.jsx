/**
 * Archivo: frontend/src/main.jsx
 * Propósito: Punto de entrada de React: monta la aplicación en el DOM.
 *
 * Historias de usuario:
 * - HU-01 · Acceso, configuración inicial y sesión segura
 */
import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import './styles.css';

createRoot(document.getElementById('root')).render(<React.StrictMode><App /></React.StrictMode>);
