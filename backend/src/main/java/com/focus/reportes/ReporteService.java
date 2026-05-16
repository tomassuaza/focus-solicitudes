package com.focus.reportes;

import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.solicitudes.SolicitudRepository;
import com.focus.tareas.Tarea;
import com.focus.tareas.TareaRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reportes operativos por periodo, unidad, categoria y tipo (TRD §6, F15 + PRD §6).
 */
@Service
public class ReporteService {

    private final SolicitudRepository solicitudRepo;
    private final TareaRepository tareaRepo;

    public ReporteService(SolicitudRepository solicitudRepo, TareaRepository tareaRepo) {
        this.solicitudRepo = solicitudRepo;
        this.tareaRepo = tareaRepo;
    }

    @Transactional(readOnly = true)
    public ReporteResponse generar(Instant desde, Instant hasta,
                                    UnidadProductiva unidad, TipoSolicitud tipo) {
        var solicitudes = solicitudRepo.buscar(desde, hasta, unidad, tipo);
        var tareas = tareaRepo.buscar(unidad, null);

        Map<String, Long> porTipo = new HashMap<>();
        Map<String, Long> porUnidad = new HashMap<>();
        Map<String, Long> porPrioridad = new HashMap<>();

        for (var s : solicitudes) {
            porTipo.merge(s.getTipo().name(), 1L, Long::sum);
            porUnidad.merge(s.getUnidad().getNombre().name(), 1L, Long::sum);
            porPrioridad.merge(s.getPrioridad().name(), 1L, Long::sum);
        }

        long tiempoTotalMin = tareas.stream()
            .map(Tarea::getTiempoRealMinutos)
            .filter(t -> t != null)
            .mapToInt(Integer::intValue)
            .sum();

        long tareasConTiempo = tareas.stream()
            .filter(t -> t.getTiempoRealMinutos() != null)
            .count();

        double porcentajeConTiempo = tareas.isEmpty() ? 0.0
            : (tareasConTiempo * 100.0) / tareas.size();

        return new ReporteResponse(
            (long) solicitudes.size(),
            (long) tareas.size(),
            porTipo,
            porUnidad,
            porPrioridad,
            tiempoTotalMin,
            Math.round(porcentajeConTiempo * 100.0) / 100.0);
    }

    public record ReporteResponse(
        Long totalSolicitudes,
        Long totalTareas,
        Map<String, Long> porTipo,
        Map<String, Long> porUnidad,
        Map<String, Long> porPrioridad,
        Long tiempoTotalMinutos,
        Double porcentajeTareasConTiempo) { }
}
