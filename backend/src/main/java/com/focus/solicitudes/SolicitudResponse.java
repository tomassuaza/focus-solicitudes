package com.focus.solicitudes;

import com.focus.common.EstadoSolicitud;
import com.focus.common.Prioridad;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import java.time.Instant;

public record SolicitudResponse(
    Long id,
    String cliente,
    UnidadProductiva unidad,
    String creador,
    TipoSolicitud tipo,
    Prioridad prioridad,
    String descripcion,
    EstadoSolicitud estado,
    Instant plazo,
    Instant fechaCreacion) {

    public static SolicitudResponse de(Solicitud s) {
        return new SolicitudResponse(
            s.getId(),
            s.getCliente().getNombre(),
            s.getUnidad().getNombre(),
            s.getCreador().getEmail(),
            s.getTipo(),
            s.getPrioridad(),
            s.getDescripcion(),
            s.getEstado(),
            s.getPlazo(),
            s.getFechaCreacion());
    }
}
