import { useState } from 'react';
import { reportesApi } from '../services/api.js';

const UNIDADES = ['', 'DISENO', 'VIDEO', 'TRAFFICKER', 'SOCIAL_MEDIA', 'ADMINISTRATIVO'];
const TIPOS = ['', 'MENSUAL', 'ADICIONAL', 'PROYECTO_ESPECIAL', 'PUNTUAL', 'URGENCIA'];

export default function Reportes() {
  const [filtros, setFiltros] = useState({ desde: '', hasta: '', unidad: '', tipo: '' });
  const [reporte, setReporte] = useState(null);
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);

  async function generar() {
    setError(null);
    setCargando(true);
    try {
      const payload = {
        ...filtros,
        desde: filtros.desde ? new Date(filtros.desde).toISOString() : null,
        hasta: filtros.hasta ? new Date(filtros.hasta).toISOString() : null,
      };
      const r = await reportesApi.generar(payload);
      setReporte(r);
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <div>
      <h1>Reportes</h1>
      <p className="muted">
        Consulta operativa por periodo, unidad productiva y tipo de solicitud.
      </p>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="desde">Desde</label>
            <input
              id="desde"
              type="date"
              value={filtros.desde}
              onChange={(e) => setFiltros((f) => ({ ...f, desde: e.target.value }))}
            />
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="hasta">Hasta</label>
            <input
              id="hasta"
              type="date"
              value={filtros.hasta}
              onChange={(e) => setFiltros((f) => ({ ...f, hasta: e.target.value }))}
            />
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="unidadR">Unidad</label>
            <select
              id="unidadR"
              value={filtros.unidad}
              onChange={(e) => setFiltros((f) => ({ ...f, unidad: e.target.value }))}
            >
              {UNIDADES.map((u) => <option key={u} value={u}>{u || '— todas —'}</option>)}
            </select>
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label htmlFor="tipoR">Tipo</label>
            <select
              id="tipoR"
              value={filtros.tipo}
              onChange={(e) => setFiltros((f) => ({ ...f, tipo: e.target.value }))}
            >
              {TIPOS.map((t) => <option key={t} value={t}>{t || '— todos —'}</option>)}
            </select>
          </div>
          <button className="btn" onClick={generar} disabled={cargando}>
            {cargando ? 'Generando...' : 'Generar'}
          </button>
        </div>
      </div>

      {reporte && (
        <div>
          <div className="card">
            <h3>Totales</h3>
            <p>Solicitudes: <strong>{reporte.totalSolicitudes}</strong></p>
            <p>Tareas: <strong>{reporte.totalTareas}</strong></p>
            <p>Tiempo total registrado: <strong>{reporte.tiempoTotalMinutos} min</strong></p>
            <p>% tareas con tiempo: <strong>{reporte.porcentajeTareasConTiempo}%</strong></p>
          </div>

          <DistribucionTabla titulo="Por tipo" data={reporte.porTipo} />
          <DistribucionTabla titulo="Por unidad" data={reporte.porUnidad} />
          <DistribucionTabla titulo="Por prioridad" data={reporte.porPrioridad} />
        </div>
      )}
    </div>
  );
}

function DistribucionTabla({ titulo, data }) {
  const entradas = Object.entries(data || {});
  return (
    <div className="card">
      <h3 style={{ marginTop: 0 }}>{titulo}</h3>
      {entradas.length === 0 ? (
        <p className="muted">Sin datos.</p>
      ) : (
        <table className="table">
          <thead><tr><th>Categoria</th><th>Cantidad</th></tr></thead>
          <tbody>
            {entradas.map(([k, v]) => <tr key={k}><td>{k}</td><td>{v}</td></tr>)}
          </tbody>
        </table>
      )}
    </div>
  );
}
