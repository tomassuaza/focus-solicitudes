import { useEffect, useState } from 'react';
import { tareasApi, solicitudesApi } from '../services/api.js';
import { validarCierreTarea } from '../utils/validators.js';

const UNIDADES = ['', 'DISENO', 'VIDEO', 'TRAFFICKER', 'SOCIAL_MEDIA', 'ADMINISTRATIVO'];
const ESTADOS = ['', 'PENDIENTE', 'EN_CURSO', 'COMPLETADA', 'CANCELADA'];

export default function MisTareas() {
  const [filtros, setFiltros] = useState({ unidad: '', estado: '' });
  const [tareas, setTareas] = useState([]);
  const [solicitudesElegibles, setSolicitudesElegibles] = useState([]);
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [crearForm, setCrearForm] = useState({ solicitudId: '' });

  async function cargar() {
    setCargando(true);
    setError(null);
    try {
      const [tareasData, solicitudesData] = await Promise.all([
        tareasApi.listar(filtros),
        solicitudesApi.listar(),
      ]);
      setTareas(tareasData || []);
      // Solicitudes en estado REGISTRADA o APROBADA pueden volverse tarea
      const elegibles = (solicitudesData || []).filter(
        (s) => s.estado === 'REGISTRADA' || s.estado === 'APROBADA'
      );
      setSolicitudesElegibles(elegibles);
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => { cargar(); /* eslint-disable-line react-hooks/exhaustive-deps */ }, []);

  async function iniciar(id) {
    setError(null);
    try {
      await tareasApi.cambiarEstado(id, 'EN_CURSO', 'Iniciando ejecucion');
      cargar();
    } catch (e) { setError(e.message); }
  }

  async function cerrar(id) {
    const valor = window.prompt('Tiempo real invertido (minutos):');
    const errMsg = validarCierreTarea(valor);
    if (errMsg) { setError(errMsg); return; }
    try {
      await tareasApi.cerrar(id, Number(valor));
      cargar();
    } catch (e) { setError(e.message); }
  }

  async function crearDesdeSolicitud(e) {
    e.preventDefault();
    setError(null);
    if (!crearForm.solicitudId) { setError('Indica el ID de solicitud'); return; }
    try {
      // Si la solicitud requiere aprobacion, primero aprobamos (caso demo).
      const s = await solicitudesApi.obtener(Number(crearForm.solicitudId));
      if (s.estado === 'PENDIENTE_APROBACION') {
        await solicitudesApi.aprobar(s.id);
      }
      await tareasApi.crear(Number(crearForm.solicitudId), null);
      setCrearForm({ solicitudId: '' });
      cargar();
    } catch (e) { setError(e.message); }
  }

  return (
    <div>
      <h1>Tareas</h1>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Filtros</h3>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="unidad">Unidad</label>
            <select
              id="unidad"
              value={filtros.unidad}
              onChange={(e) => setFiltros((f) => ({ ...f, unidad: e.target.value }))}
            >
              {UNIDADES.map((u) => <option key={u} value={u}>{u || '— todas —'}</option>)}
            </select>
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="estado">Estado</label>
            <select
              id="estado"
              value={filtros.estado}
              onChange={(e) => setFiltros((f) => ({ ...f, estado: e.target.value }))}
            >
              {ESTADOS.map((s) => <option key={s} value={s}>{s || '— todos —'}</option>)}
            </select>
          </div>
          <button className="btn" onClick={cargar} disabled={cargando}>
            {cargando ? 'Cargando...' : 'Aplicar'}
          </button>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Crear tarea desde solicitud</h3>
        <p className="muted" style={{ marginTop: 0 }}>
          Selecciona una solicitud REGISTRADA o APROBADA para convertirla en tarea ejecutable.
        </p>
        {solicitudesElegibles.length === 0 ? (
          <p className="muted">
            No hay solicitudes elegibles. Crea una desde <strong>Nueva solicitud</strong> primero.
          </p>
        ) : (
          <form onSubmit={crearDesdeSolicitud} style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end' }}>
            <div className="field" style={{ marginBottom: 0, flex: 1 }}>
              <label htmlFor="sid">Solicitud</label>
              <select
                id="sid"
                value={crearForm.solicitudId}
                onChange={(e) => setCrearForm({ solicitudId: e.target.value })}
              >
                <option value="">— elige una solicitud —</option>
                {solicitudesElegibles.map((s) => (
                  <option key={s.id} value={s.id}>
                    #{s.id} · {s.cliente} · {s.tipo} · {s.unidad} · {s.descripcion.slice(0, 50)}
                  </option>
                ))}
              </select>
            </div>
            <button className="btn" type="submit" disabled={!crearForm.solicitudId}>
              Crear tarea
            </button>
          </form>
        )}
      </div>

      <table className="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Solicitud</th>
            <th>Cliente</th>
            <th>Estado</th>
            <th>Tiempo (min)</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {tareas.length === 0 && (
            <tr><td colSpan={6} className="muted">Sin tareas para el filtro actual.</td></tr>
          )}
          {tareas.map((t) => (
            <tr key={t.id}>
              <td>{t.id}</td>
              <td>{t.solicitudId}</td>
              <td>{t.cliente}</td>
              <td><span className={`badge badge-${t.estado}`}>{t.estado}</span></td>
              <td>{t.tiempoRealMinutos ?? '—'}</td>
              <td>
                {t.estado === 'PENDIENTE' && (
                  <button className="btn" onClick={() => iniciar(t.id)}>Iniciar</button>
                )}
                {t.estado === 'EN_CURSO' && (
                  <button className="btn" onClick={() => cerrar(t.id)}>Cerrar</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
