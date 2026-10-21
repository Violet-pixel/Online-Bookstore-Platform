// Cliente da API. O token de acesso fica só na memória; a renovação usa o cookie HttpOnly.
let accessToken = null;
let refreshing = null;
const listeners = new Set();

export function setAccessToken(token) {
  accessToken = token;
}

export function onSessionExpired(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.detail || 'Não foi possível concluir. Tente de novo.');
    this.status = status;
    this.code = body?.code;
    this.fields = body?.fields || {};
  }
}

async function refresh() {
  if (!refreshing) {
    refreshing = fetch('/api/auth/refresh', { method: 'POST', credentials: 'same-origin' })
      .then(async (r) => {
        if (!r.ok) throw new ApiError(r.status, await r.json().catch(() => null));
        const data = await r.json();
        accessToken = data.accessToken;
        return data;
      })
      .finally(() => {
        refreshing = null;
      });
  }
  return refreshing;
}

const FLAG = 'prelo:sessao';
export function rememberSession(on) {
  try {
    if (on) localStorage.setItem(FLAG, '1');
    else localStorage.removeItem(FLAG);
  } catch { /* sem armazenamento */ }
}

/** Só tenta renovar quando este navegador já entrou antes (evita um 401 à toa a cada visita). */
export async function tryRestoreSession() {
  let had = true;
  try { had = localStorage.getItem(FLAG) === '1'; } catch { /* sem armazenamento: tenta mesmo assim */ }
  if (!had) return null;
  try {
    return await refresh();
  } catch {
    rememberSession(false);
    return null;
  }
}

export async function api(path, { method = 'GET', body, headers = {}, retry = true } = {}) {
  const init = {
    method,
    credentials: 'same-origin',
    headers: { Accept: 'application/json', ...headers },
  };
  if (body !== undefined) {
    init.headers['Content-Type'] = 'application/json';
    init.body = JSON.stringify(body);
  }
  if (accessToken) init.headers.Authorization = `Bearer ${accessToken}`;
  let res;
  try {
    res = await fetch(path, init);
  } catch {
    throw new ApiError(0, { detail: 'Sem conexão com a livraria. Confira sua internet.' });
  }
  if (res.status === 401 && retry && !path.startsWith('/api/auth/')) {
    try {
      await refresh();
      return api(path, { method, body, headers, retry: false });
    } catch {
      accessToken = null;
      listeners.forEach((fn) => fn());
    }
  }
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}

export const get = (p) => api(p);
export const post = (p, body, headers) => api(p, { method: 'POST', body, headers });
export const put = (p, body) => api(p, { method: 'PUT', body });
export const patch = (p, body) => api(p, { method: 'PATCH', body });
export const del = (p) => api(p, { method: 'DELETE' });

export function qs(params) {
  const s = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '' && v !== false) s.set(k, v);
  });
  const str = s.toString();
  return str ? `?${str}` : '';
}
