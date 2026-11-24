const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

export async function api(path, options = {}) {
  const token = localStorage.getItem('smarthire_token');
  const headers = new Headers(options.headers || {});
  if (token) headers.set('Authorization', `Bearer ${token}`);
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const response = await fetch(`${API_URL}${path}`, { ...options, headers });
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
