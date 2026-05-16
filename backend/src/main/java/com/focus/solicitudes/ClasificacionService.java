package com.focus.solicitudes;

import com.focus.common.Prioridad;
import com.focus.common.TipoCliente;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import org.springframework.stereotype.Service;

/**
 * Reglas de clasificacion y asignacion automatica (Design Doc §8 / TRD §6).
 *
 * Las reglas son intencionadamente simples (deuda tecnica aceptada en Design Doc §16):
 *  - El tipo se respeta del input pero se *corrige* si es incoherente con el cliente
 *    (un cliente PUNTUAL no puede tener tipo MENSUAL).
 *  - La unidad se sugiere segun palabras clave en la descripcion cuando no se
 *    indica explicitamente.
 *  - URGENCIA siempre sube la prioridad a CRITICA si llega como BAJA o MEDIA.
 */
@Service
public class ClasificacionService {

    public ResultadoClasificacion clasificar(TipoSolicitud tipoSolicitado,
                                              TipoCliente tipoCliente,
                                              Prioridad prioridadSolicitada,
                                              UnidadProductiva unidadSugerida,
                                              String descripcion) {
        TipoSolicitud tipoFinal = resolverTipo(tipoSolicitado, tipoCliente);
        Prioridad prioridadFinal = resolverPrioridad(tipoFinal, prioridadSolicitada);
        UnidadProductiva unidadFinal = unidadSugerida != null
            ? unidadSugerida
            : inferirUnidad(descripcion);
        return new ResultadoClasificacion(tipoFinal, prioridadFinal, unidadFinal);
    }

    TipoSolicitud resolverTipo(TipoSolicitud tipoSolicitado, TipoCliente tipoCliente) {
        if (tipoSolicitado == TipoSolicitud.MENSUAL && tipoCliente != TipoCliente.MENSUAL) {
            return TipoSolicitud.PUNTUAL;
        }
        return tipoSolicitado;
    }

    Prioridad resolverPrioridad(TipoSolicitud tipo, Prioridad p) {
        if (tipo == TipoSolicitud.URGENCIA && (p == Prioridad.BAJA || p == Prioridad.MEDIA)) {
            return Prioridad.CRITICA;
        }
        return p;
    }

    UnidadProductiva inferirUnidad(String descripcion) {
        if (descripcion == null) {
            return UnidadProductiva.ADMINISTRATIVO;
        }
        String d = descripcion.toLowerCase();
        if (d.contains("video") || d.contains("edicion") || d.contains("grabacion") || d.contains("filmar")) {
            return UnidadProductiva.VIDEO;
        }
        if (d.contains("diseño") || d.contains("diseno") || d.contains("logo")
            || d.contains("branding") || d.contains("pieza") || d.contains("ilustra")) {
            return UnidadProductiva.DISENO;
        }
        if (d.contains("ads") || d.contains("pauta") || d.contains("campaña")
            || d.contains("campana") || d.contains("traffic")) {
            return UnidadProductiva.TRAFFICKER;
        }
        if (d.contains("instagram") || d.contains("tiktok") || d.contains("facebook")
            || d.contains("redes") || d.contains("post") || d.contains("reel")) {
            return UnidadProductiva.SOCIAL_MEDIA;
        }
        return UnidadProductiva.ADMINISTRATIVO;
    }

    public record ResultadoClasificacion(
        TipoSolicitud tipo,
        Prioridad prioridad,
        UnidadProductiva unidad) { }
}
