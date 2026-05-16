package com.focus.tareas;

import com.focus.common.EstadoTarea;
import java.time.Instant;

public record TareaResponse(
    Long id,
    Long solicitudId,
    String cliente,
    String responsable,
    EstadoTarea estado,
    Integer tiempoRealMinutos,
    Instant fechaInicio,
    Instant fechaCierre) {

    public static TareaResponse de(Tarea t) {
        return new TareaResponse(
            t.getId(),
            t.getSolicitud().getId(),
            t.getSolicitud().getCliente().getNombre(),
            t.getResponsable() != null ? t.getResponsable().getEmail() : null,
            t.getEstado(),
            t.getTiempoRealMinutos(),
            t.getFechaInicio(),
            t.getFechaCierre());
    }
}
