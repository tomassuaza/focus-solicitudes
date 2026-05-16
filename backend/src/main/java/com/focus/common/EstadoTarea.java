package com.focus.common;

/**
 * Ciclo de vida de una tarea segun TRD §6 y Design Doc §8.
 */
public enum EstadoTarea {
    PENDIENTE,
    EN_CURSO,
    COMPLETADA,
    CANCELADA;

    public boolean puedeTransicionarA(EstadoTarea nuevo) {
        return switch (this) {
            case PENDIENTE  -> nuevo == EN_CURSO   || nuevo == CANCELADA;
            case EN_CURSO   -> nuevo == COMPLETADA || nuevo == CANCELADA;
            case COMPLETADA -> false;
            case CANCELADA  -> false;
        };
    }
}
