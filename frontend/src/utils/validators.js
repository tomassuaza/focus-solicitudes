/**
 * Validador de formulario de nueva solicitud.
 * Replica las validaciones server-side para feedback inmediato (TRD F4).
 */
export function validarNuevaSolicitud(data) {
  const errores = {};
  if (!data.clienteId) errores.clienteId = 'Selecciona un cliente';
  if (!data.tipo) errores.tipo = 'El tipo de solicitud es obligatorio';
  if (!data.prioridad) errores.prioridad = 'La prioridad es obligatoria';
  if (!data.descripcion || data.descripcion.trim().length === 0) {
    errores.descripcion = 'La descripcion es obligatoria';
  } else if (data.descripcion.length > 2000) {
    errores.descripcion = 'Maximo 2000 caracteres';
  }
  return errores;
}

/**
 * Validador para el cierre de tarea (TRD F12 - tiempo real obligatorio).
 */
export function validarCierreTarea(tiempoMin) {
  if (tiempoMin == null || tiempoMin === '') return 'Registra el tiempo real invertido';
  const n = Number(tiempoMin);
  if (Number.isNaN(n) || !Number.isInteger(n)) return 'Debe ser un numero entero de minutos';
  if (n <= 0) return 'El tiempo debe ser mayor a 0';
  if (n > 60 * 24 * 30) return 'Tiempo demasiado grande';
  return null;
}
