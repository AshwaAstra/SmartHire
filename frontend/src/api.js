const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

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
    if (error?.name === 'AbortError') {
      throw new Error(`The backend did not respond in time. Check the server and database connection at ${API_URL}.`);
    }
    if (error instanceof TypeError) {
      throw new Error(`Cannot reach the backend at ${API_URL}. Check that it is running and allows this frontend origin.`);
    }
    throw error;
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
