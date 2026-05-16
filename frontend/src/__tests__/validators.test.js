import { describe, it, expect } from 'vitest';
import { validarNuevaSolicitud, validarCierreTarea } from '../utils/validators.js';

describe('validarNuevaSolicitud', () => {
  it('acepta un formulario completo y valido', () => {
    const errores = validarNuevaSolicitud({
      clienteId: 1, tipo: 'MENSUAL', prioridad: 'MEDIA',
      descripcion: 'banner del mes',
    });
    expect(errores).toEqual({});
  });

  it('reporta clienteId faltante', () => {
    const errores = validarNuevaSolicitud({
      clienteId: null, tipo: 'MENSUAL', prioridad: 'MEDIA', descripcion: 'x',
    });
    expect(errores.clienteId).toBeDefined();
  });

  it('reporta descripcion vacia', () => {
    const errores = validarNuevaSolicitud({
      clienteId: 1, tipo: 'MENSUAL', prioridad: 'MEDIA', descripcion: '   ',
    });
    expect(errores.descripcion).toBeDefined();
  });

  it('reporta descripcion superior a 2000 caracteres', () => {
    const errores = validarNuevaSolicitud({
      clienteId: 1, tipo: 'MENSUAL', prioridad: 'MEDIA',
      descripcion: 'a'.repeat(2001),
    });
    expect(errores.descripcion).toMatch(/2000/);
  });

  it('reporta tipo y prioridad faltantes', () => {
    const errores = validarNuevaSolicitud({
      clienteId: 1, tipo: null, prioridad: null, descripcion: 'x',
    });
    expect(errores.tipo).toBeDefined();
    expect(errores.prioridad).toBeDefined();
  });
});

describe('validarCierreTarea', () => {
  it('acepta numero entero positivo', () => {
    expect(validarCierreTarea(120)).toBeNull();
    expect(validarCierreTarea('45')).toBeNull();
  });

  it('rechaza valor vacio', () => {
    expect(validarCierreTarea('')).toMatch(/tiempo real/);
    expect(validarCierreTarea(null)).toMatch(/tiempo real/);
  });

  it('rechaza decimales', () => {
    expect(validarCierreTarea('1.5')).toMatch(/entero/);
  });

  it('rechaza 0 o negativo', () => {
    expect(validarCierreTarea(0)).toMatch(/mayor a 0/);
    expect(validarCierreTarea(-5)).toMatch(/mayor a 0/);
  });

  it('rechaza valores absurdamente grandes', () => {
    expect(validarCierreTarea(60 * 24 * 31)).toMatch(/grande/);
  });
});
