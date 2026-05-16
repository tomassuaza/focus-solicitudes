package com.focus.tareas;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CerrarTareaRequest(@NotNull @Positive Integer tiempoRealMinutos) { }
