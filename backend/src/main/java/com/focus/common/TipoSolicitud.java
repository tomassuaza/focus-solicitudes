package com.focus.common;

/**
 * Tipos de trabajo segun PRD y RFC.
 * Algunos (ADICIONAL, URGENCIA) requieren aprobacion de Direccion antes de ejecutarse.
 */
public enum TipoSolicitud {
    MENSUAL,
    ADICIONAL,
    PROYECTO_ESPECIAL,
    PUNTUAL,
    URGENCIA;

    public boolean requiereAprobacion() {
        return this == ADICIONAL || this == URGENCIA;
    }
}
