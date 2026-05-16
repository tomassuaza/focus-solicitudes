package com.focus.tareas;

import com.focus.common.ApiException;
import com.focus.common.EstadoSolicitud;
import com.focus.common.EstadoTarea;
import com.focus.common.UnidadProductiva;
import com.focus.notificaciones.NotificacionService;
import com.focus.solicitudes.Solicitud;
import com.focus.solicitudes.SolicitudRepository;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TareaService {

    private static final Logger log = LoggerFactory.getLogger(TareaService.class);

    private final TareaRepository tareaRepo;
    private final HistorialEstadoRepository historialRepo;
    private final SolicitudRepository solicitudRepo;
    private final UsuarioRepository usuarioRepo;
    private final NotificacionService notificaciones;

    public TareaService(TareaRepository tareaRepo,
                         HistorialEstadoRepository historialRepo,
                         SolicitudRepository solicitudRepo,
                         UsuarioRepository usuarioRepo,
                         NotificacionService notificaciones) {
        this.tareaRepo = tareaRepo;
        this.historialRepo = historialRepo;
        this.solicitudRepo = solicitudRepo;
        this.usuarioRepo = usuarioRepo;
        this.notificaciones = notificaciones;
    }

    /**
     * Crea la tarea asociada a una solicitud. Solo se permite si la solicitud esta
     * REGISTRADA o APROBADA. Cumple TRD §6: no se puede crear una tarea sin solicitud previa.
     */
    @Transactional
    public Tarea crearDesdeSolicitud(Long solicitudId, Long responsableId) {
        Solicitud s = solicitudRepo.findById(solicitudId)
            .orElseThrow(() -> ApiException.notFound("Solicitud", solicitudId));

        if (s.getEstado() != EstadoSolicitud.REGISTRADA && s.getEstado() != EstadoSolicitud.APROBADA) {
            throw ApiException.badRequest(
                "No se puede crear tarea para solicitud en estado " + s.getEstado());
        }

        Tarea t = new Tarea(s);
        if (responsableId != null) {
            Usuario u = usuarioRepo.findById(responsableId)
                .orElseThrow(() -> ApiException.notFound("Usuario", responsableId));
            t.setResponsable(u);
        }
        Tarea guardada = tareaRepo.save(t);
        log.info("Tarea creada id={} desde solicitud {}", guardada.getId(), solicitudId);
        return guardada;
    }

    @Transactional
    public Tarea cambiarEstado(Long tareaId, EstadoTarea nuevoEstado, Usuario quien, String motivo) {
        Tarea t = tareaRepo.findById(tareaId)
            .orElseThrow(() -> ApiException.notFound("Tarea", tareaId));

        if (!t.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw ApiException.badRequest(
                "Transicion invalida: " + t.getEstado() + " -> " + nuevoEstado);
        }

        EstadoTarea anterior = t.getEstado();
        t.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoTarea.EN_CURSO && t.getFechaInicio() == null) {
            t.setFechaInicio(Instant.now());
        }
        historialRepo.save(new HistorialEstado(t, anterior, nuevoEstado, quien, motivo));
        notificaciones.notificarCambioEstado(t);
        return tareaRepo.save(t);
    }

    /**
     * Cierra la tarea registrando tiempo real invertido (TRD §6, F12).
     * Requisito explicito del PRD: registro de tiempo real al cierre.
     */
    @Transactional
    public Tarea cerrar(Long tareaId, Integer tiempoRealMinutos, Usuario quien) {
        Tarea t = tareaRepo.findById(tareaId)
            .orElseThrow(() -> ApiException.notFound("Tarea", tareaId));

        if (t.getEstado() != EstadoTarea.EN_CURSO) {
            throw ApiException.badRequest("Solo se pueden cerrar tareas en estado EN_CURSO");
        }
        if (tiempoRealMinutos == null || tiempoRealMinutos <= 0) {
            throw ApiException.badRequest("Debe registrar el tiempo real invertido");
        }

        t.setTiempoRealMinutos(tiempoRealMinutos);
        t.setFechaCierre(Instant.now());
        EstadoTarea anterior = t.getEstado();
        t.setEstado(EstadoTarea.COMPLETADA);
        historialRepo.save(new HistorialEstado(t, anterior, EstadoTarea.COMPLETADA, quien,
            "Cierre con " + tiempoRealMinutos + " min"));
        return tareaRepo.save(t);
    }

    @Transactional(readOnly = true)
    public List<Tarea> buscar(UnidadProductiva unidad, EstadoTarea estado) {
        return tareaRepo.buscar(unidad, estado);
    }

    @Transactional(readOnly = true)
    public Tarea porId(Long id) {
        return tareaRepo.findById(id).orElseThrow(() -> ApiException.notFound("Tarea", id));
    }
}
