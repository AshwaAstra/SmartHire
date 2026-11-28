// A few hosting dashboards escape `:` as `\:` when values are copied from
// shell examples. Browsers treat that as a malformed fetch URL, so normalize
// the configured base once and share it with callers that perform file calls.
export const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8081')
  .replaceAll('\\', '')
  .replace(/\/$/, '');

export async function api(path, options = {}) {
  const token = localStorage.getItem('smarthire_token');
  const headers = new Headers(options.headers || {});
  if (token) headers.set('Authorization', `Bearer ${token}`);
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  let response;
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers });
  } catch (error) {
    const method = (options.method || 'GET').toUpperCase();
    // Render can briefly refuse the first connection while a free instance wakes.
    // Retry only read-only requests so posting, saving, and deleting are never duplicated.
    if (error instanceof TypeError && (method === 'GET' || method === 'HEAD')) {
      await new Promise((resolve) => window.setTimeout(resolve, 1200));
      try {
        response = await fetch(`${API_URL}${path}`, { ...options, headers });
      } catch (retryError) {
        error = retryError;
      }
    }
    if (response) {
      // Continue through the normal HTTP status and JSON handling below.
    } else if (error?.name === 'AbortError') {
      throw new Error(`The backend did not respond in time. Check the server and database connection at ${API_URL}.`);
    } else if (error instanceof TypeError) {
      throw new Error(`Cannot reach the backend at ${API_URL} from ${window.location.origin}. Check that it is running and this exact origin is allowed in CORS_ALLOWED_ORIGINS.`);
    } else {
      throw error;
    }
  }
  if (response.status === 401 && token) {
    localStorage.removeItem('smarthire_token');
    localStorage.removeItem('smarthire_user');
    if (window.location.pathname !== '/login') window.location.assign('/login?reason=session-expired');
  }
  if (!response.ok) {
    let message = `Request failed (${response.status} ${response.statusText})`;
    try {
      const body = await response.json();
      message = body.error || body.message || body.detail || message;
    } catch {}
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}
