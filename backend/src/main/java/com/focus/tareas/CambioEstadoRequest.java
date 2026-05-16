package com.focus.tareas;

import com.focus.common.EstadoTarea;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull EstadoTarea nuevoEstado, String motivo) { }
