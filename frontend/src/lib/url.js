// $lib/url.js

// Backend API host
export function getApiHost() {
  // Use env variable if set (ngrok / production)
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }

  // Otherwise, use current host + port
  const protocol = location.protocol === 'https:' ? 'https' : 'http';
  const host = location.hostname;
  const port = location.port ? location.port : '8080';
  return `${protocol}://${host}:${port}`;
}

// WebSocket host
export function getWebSocketHost() {
  if (import.meta.env.VITE_API_URL) {
    const url = new URL(import.meta.env.VITE_API_URL);
    const protocol = url.protocol === 'https:' ? 'wss' : 'ws';
    return `${protocol}://${url.host}`;
  }

  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  const host = location.hostname;
  const port = location.port ? location.port : '8080';
  return `${protocol}://${host}:${port}`;
}

// Frontend base URL
export function getFrontendHost() {
  return window.location.origin
}
