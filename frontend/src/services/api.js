const BASE = import.meta.env.VITE_API_BASE_URL || '';

function authHeaders() {
  const token = localStorage.getItem('focus_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...(options.headers || {}),
    },
  });
  if (!res.ok) {
    let mensaje = `Error ${res.status}`;
    try {
      const body = await res.json();
      mensaje = body.mensaje || mensaje;
    } catch (_) {
      // body no es JSON, ignorar
    }
    const err = new Error(mensaje);
    err.status = res.status;
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

// === Auth ===
export const authApi = {
  loginGoogle: (idToken) =>
    request('/api/auth/google', { method: 'POST', body: JSON.stringify({ idToken }) }),
  loginDemo: (rol = 'COORDINADOR') =>
    request(`/api/auth/demo?rol=${encodeURIComponent(rol)}`),
};

// === Solicitudes ===
export const solicitudesApi = {
  crear: (data) =>
    request('/api/solicitudes', { method: 'POST', body: JSON.stringify(data) }),
  listar: (filtros = {}) => {
    const qs = new URLSearchParams(
      Object.entries(filtros).filter(([, v]) => v != null && v !== '')
    ).toString();
    return request(`/api/solicitudes${qs ? `?${qs}` : ''}`);
  },
  obtener: (id) => request(`/api/solicitudes/${id}`),
  reclasificar: (id, data) =>
    request(`/api/solicitudes/${id}/reclasificar`, { method: 'PUT', body: JSON.stringify(data) }),
  aprobar: (id) => request(`/api/solicitudes/${id}/aprobar`, { method: 'POST' }),
  rechazar: (id) => request(`/api/solicitudes/${id}/rechazar`, { method: 'POST' }),
};

// === Tareas ===
export const tareasApi = {
  listar: (filtros = {}) => {
    const qs = new URLSearchParams(
      Object.entries(filtros).filter(([, v]) => v != null && v !== '')
    ).toString();
    return request(`/api/tareas${qs ? `?${qs}` : ''}`);
  },
  crear: (solicitudId, responsableId) => {
    const qs = new URLSearchParams({ solicitudId, ...(responsableId && { responsableId }) }).toString();
    return request(`/api/tareas?${qs}`, { method: 'POST' });
  },
  cambiarEstado: (id, nuevoEstado, motivo) =>
    request(`/api/tareas/${id}/estado`, { method: 'PUT', body: JSON.stringify({ nuevoEstado, motivo }) }),
  cerrar: (id, tiempoRealMinutos) =>
    request(`/api/tareas/${id}/cerrar`, { method: 'PUT', body: JSON.stringify({ tiempoRealMinutos }) }),
};

// === Reportes ===
export const reportesApi = {
  generar: (filtros = {}) => {
    const qs = new URLSearchParams(
      Object.entries(filtros).filter(([, v]) => v != null && v !== '')
    ).toString();
    return request(`/api/reportes${qs ? `?${qs}` : ''}`);
  },
};
