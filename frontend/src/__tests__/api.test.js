import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { solicitudesApi, tareasApi } from '../services/api.js';

describe('servicios API', () => {
  beforeEach(() => {
    localStorage.clear();
    globalThis.fetch = vi.fn();
  });
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('envia Authorization Bearer cuando hay token', async () => {
    localStorage.setItem('focus_token', 'abc123');
    globalThis.fetch.mockResolvedValueOnce({
      ok: true, status: 200,
      json: async () => ([]),
    });

    await solicitudesApi.listar();

    const [, opts] = globalThis.fetch.mock.calls[0];
    expect(opts.headers.Authorization).toBe('Bearer abc123');
  });

  it('no envia Authorization si no hay token', async () => {
    globalThis.fetch.mockResolvedValueOnce({
      ok: true, status: 200,
      json: async () => ([]),
    });

    await solicitudesApi.listar();

    const [, opts] = globalThis.fetch.mock.calls[0];
    expect(opts.headers.Authorization).toBeUndefined();
  });

  it('lanza Error con mensaje del body cuando la respuesta no es OK', async () => {
    globalThis.fetch.mockResolvedValueOnce({
      ok: false, status: 400,
      json: async () => ({ mensaje: 'Validacion fallida' }),
    });

    await expect(solicitudesApi.crear({})).rejects.toThrow('Validacion fallida');
  });

  it('cerrar tarea hace PUT a /api/tareas/{id}/cerrar', async () => {
    globalThis.fetch.mockResolvedValueOnce({
      ok: true, status: 200,
      json: async () => ({ id: 1 }),
    });

    await tareasApi.cerrar(7, 90);

    const [url, opts] = globalThis.fetch.mock.calls[0];
    expect(url).toMatch(/\/api\/tareas\/7\/cerrar$/);
    expect(opts.method).toBe('PUT');
    expect(JSON.parse(opts.body)).toEqual({ tiempoRealMinutos: 90 });
  });

  it('listar pasa los filtros como query string', async () => {
    globalThis.fetch.mockResolvedValueOnce({
      ok: true, status: 200,
      json: async () => ([]),
    });

    await solicitudesApi.listar({ unidad: 'DISENO', tipo: 'MENSUAL' });

    const [url] = globalThis.fetch.mock.calls[0];
    expect(url).toContain('unidad=DISENO');
    expect(url).toContain('tipo=MENSUAL');
  });
});
