import { useState } from 'react';
import { solicitudesApi } from '../services/api.js';
import { validarNuevaSolicitud } from '../utils/validators.js';

const TIPOS = ['MENSUAL', 'ADICIONAL', 'PROYECTO_ESPECIAL', 'PUNTUAL', 'URGENCIA'];
const PRIORIDADES = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA'];
const UNIDADES = ['DISENO', 'VIDEO', 'TRAFFICKER', 'SOCIAL_MEDIA', 'ADMINISTRATIVO'];

export default function NuevaSolicitud() {
  const [form, setForm] = useState({
    clienteId: 1,
    creadorId: 1,
    tipo: 'MENSUAL',
    prioridad: 'MEDIA',
    descripcion: '',
    unidadSugerida: '',
    plazo: '',
  });
  const [errores, setErrores] = useState({});
  const [estado, setEstado] = useState({ tipo: null, mensaje: null });
  const [guardando, setGuardando] = useState(false);

  function update(campo, valor) {
    setForm((f) => ({ ...f, [campo]: valor }));
  }

  async function onSubmit(e) {
    e.preventDefault();
    setEstado({ tipo: null, mensaje: null });
    const errs = validarNuevaSolicitud(form);
    setErrores(errs);
    if (Object.keys(errs).length > 0) return;

    setGuardando(true);
    try {
      const payload = {
        ...form,
        unidadSugerida: form.unidadSugerida || null,
        plazo: form.plazo ? new Date(form.plazo).toISOString() : null,
      };
      const creada = await solicitudesApi.crear(payload);
      setEstado({
        tipo: 'ok',
        mensaje: `Solicitud #${creada.id} creada (estado: ${creada.estado}, unidad: ${creada.unidad}).`,
      });
      setForm((f) => ({ ...f, descripcion: '' }));
    } catch (err) {
      setEstado({ tipo: 'error', mensaje: err.message });
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div>
      <h1>Nueva solicitud</h1>
      <p className="muted">
        Toda solicitud debe registrarse aqui — es el unico punto oficial de ingreso.
      </p>

      {estado.mensaje && (
        <div className={estado.tipo === 'ok' ? 'alert alert-success' : 'alert alert-error'}>
          {estado.mensaje}
        </div>
      )}

      <form className="card" onSubmit={onSubmit} aria-label="Formulario de nueva solicitud">
        <div className="field">
          <label htmlFor="clienteId">Cliente (ID)</label>
          <input
            id="clienteId"
            type="number"
            value={form.clienteId}
            onChange={(e) => update('clienteId', Number(e.target.value))}
          />
          {errores.clienteId && <span className="field-error">{errores.clienteId}</span>}
        </div>

        <div className="field">
          <label htmlFor="tipo">Tipo de trabajo *</label>
          <select id="tipo" value={form.tipo} onChange={(e) => update('tipo', e.target.value)}>
            {TIPOS.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          {errores.tipo && <span className="field-error">{errores.tipo}</span>}
        </div>

        <div className="field">
          <label htmlFor="prioridad">Prioridad *</label>
          <select
            id="prioridad"
            value={form.prioridad}
            onChange={(e) => update('prioridad', e.target.value)}
          >
            {PRIORIDADES.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
          {errores.prioridad && <span className="field-error">{errores.prioridad}</span>}
        </div>

        <div className="field">
          <label htmlFor="unidad">Unidad sugerida (opcional — se infiere si se omite)</label>
          <select
            id="unidad"
            value={form.unidadSugerida}
            onChange={(e) => update('unidadSugerida', e.target.value)}
          >
            <option value="">— inferir automaticamente —</option>
            {UNIDADES.map((u) => <option key={u} value={u}>{u}</option>)}
          </select>
        </div>

        <div className="field">
          <label htmlFor="plazo">Plazo (opcional)</label>
          <input
            id="plazo"
            type="datetime-local"
            value={form.plazo}
            onChange={(e) => update('plazo', e.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="descripcion">Descripcion *</label>
          <textarea
            id="descripcion"
            value={form.descripcion}
            onChange={(e) => update('descripcion', e.target.value)}
            placeholder="Describe el trabajo solicitado..."
            maxLength={2000}
          />
          {errores.descripcion && <span className="field-error">{errores.descripcion}</span>}
        </div>

        <button className="btn" type="submit" disabled={guardando}>
          {guardando ? 'Registrando...' : 'Registrar solicitud'}
        </button>
      </form>
    </div>
  );
}
