package com.focus.solicitudes;

import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import jakarta.validation.constraints.NotNull;

public record ReclasificarRequest(
    @NotNull TipoSolicitud nuevoTipo,
    @NotNull UnidadProductiva nuevaUnidad,
    String motivo) { }
