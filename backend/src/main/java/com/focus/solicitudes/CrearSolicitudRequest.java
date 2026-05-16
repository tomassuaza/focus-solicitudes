package com.focus.solicitudes;

import com.focus.common.Prioridad;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CrearSolicitudRequest(
    @NotNull Long clienteId,
    @NotNull Long creadorId,
    @NotNull TipoSolicitud tipo,
    @NotNull Prioridad prioridad,
    @NotBlank @Size(max = 2000) String descripcion,
    UnidadProductiva unidadSugerida,
    Instant plazo) { }
