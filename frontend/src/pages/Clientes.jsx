import { useEffect, useState } from 'react';
import { clientesApi } from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

const TIPOS = ['MENSUAL', 'PUNTUAL', 'OCASIONAL'];

export default function Clientes() {
  const { user } = useAuth();
  const [clientes, setClientes] = useState([]);
  const [form, setForm] = useState({ nombre: '', tipo: 'MENSUAL' });
  const [error, setError] = useState(null);
  const [exito, setExito] = useState(null);
  const [cargando, setCargando] = useState(false);

  const puedeCrear = user?.rol === 'COORDINADOR' || user?.rol === 'DIRECCION';
  const puedeDesactivar = user?.rol === 'DIRECCION';

  async function cargar() {
    setError(null);
    try {
      const data = await clientesApi.listar();
      setClientes(data || []);
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => { cargar(); }, []);

  async function onSubmit(e) {
    e.preventDefault();
    setError(null);
    setExito(null);
    if (!form.nombre.trim()) {
      setError('El nombre del cliente es obligatorio');
      return;
    }
    setCargando(true);
    try {
      const creado = await clientesApi.crear(form);
      setExito(`Cliente "${creado.nombre}" creado (id: ${creado.id})`);
      setForm({ nombre: '', tipo: 'MENSUAL' });
      cargar();
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  }

  async function desactivar(id, nombre) {
    if (!window.confirm(`Desactivar cliente "${nombre}"? No podras crear solicitudes para el.`)) {
      return;
    }
    setError(null);
    try {
      await clientesApi.desactivar(id);
      cargar();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div>
      <h1>Clientes</h1>
      <p className="muted">Gestion de clientes de la agencia.</p>

      {error && <div className="alert alert-error">{error}</div>}
      {exito && <div className="alert alert-success">{exito}</div>}

      {puedeCrear && (
        <form className="card" onSubmit={onSubmit} aria-label="Crear cliente">
          <h3 style={{ marginTop: 0 }}>Nuevo cliente</h3>
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div className="field" style={{ marginBottom: 0, flex: 2 }}>
              <label htmlFor="nombre">Nombre *</label>
              <input
                id="nombre"
                type="text"
                value={form.nombre}
                onChange={(e) => setForm((f) => ({ ...f, nombre: e.target.value }))}
                placeholder="Ej: Cafe Quindio, Universidad del Quindio..."
                maxLength={180}
              />
            </div>
            <div className="field" style={{ marginBottom: 0, flex: 1 }}>
              <label htmlFor="tipo">Tipo *</label>
              <select
                id="tipo"
                value={form.tipo}
                onChange={(e) => setForm((f) => ({ ...f, tipo: e.target.value }))}
              >
                {TIPOS.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <button className="btn" type="submit" disabled={cargando}>
              {cargando ? 'Creando...' : 'Crear cliente'}
            </button>
          </div>
        </form>
      )}

      {!puedeCrear && (
        <div className="card">
          <p className="muted">Solo los roles COORDINADOR y DIRECCION pueden crear clientes.</p>
        </div>
      )}

      <h3>Lista de clientes activos ({clientes.length})</h3>
      <table className="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Nombre</th>
            <th>Tipo</th>
            {puedeDesactivar && <th>Acciones</th>}
          </tr>
        </thead>
        <tbody>
          {clientes.length === 0 && (
            <tr>
              <td colSpan={puedeDesactivar ? 4 : 3} className="muted">
                No hay clientes registrados.
              </td>
            </tr>
          )}
          {clientes.map((c) => (
            <tr key={c.id}>
              <td>{c.id}</td>
              <td>{c.nombre}</td>
              <td><span className="badge">{c.tipo}</span></td>
              {puedeDesactivar && (
                <td>
                  <button
                    className="btn btn-danger"
                    onClick={() => desactivar(c.id, c.nombre)}
                  >
                    Desactivar
                  </button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
