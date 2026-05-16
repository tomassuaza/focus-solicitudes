package com.focus.solicitudes;

import com.focus.clientes.Cliente;
import com.focus.clientes.ClienteRepository;
import com.focus.clientes.UnidadProductivaEntity;
import com.focus.clientes.UnidadProductivaRepository;
import com.focus.common.ApiException;
import com.focus.common.EstadoSolicitud;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.notificaciones.NotificacionService;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudService.class);

    private final SolicitudRepository repo;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final UnidadProductivaRepository unidadRepo;
    private final ClasificacionService clasificacion;
    private final NotificacionService notificaciones;

    public SolicitudService(SolicitudRepository repo,
                             ClienteRepository clienteRepo,
                             UsuarioRepository usuarioRepo,
                             UnidadProductivaRepository unidadRepo,
                             ClasificacionService clasificacion,
                             NotificacionService notificaciones) {
        this.repo = repo;
        this.clienteRepo = clienteRepo;
        this.usuarioRepo = usuarioRepo;
        this.unidadRepo = unidadRepo;
        this.clasificacion = clasificacion;
        this.notificaciones = notificaciones;
    }

    /**
     * Registra una nueva solicitud, la clasifica automaticamente, la asigna a una unidad
     * y dispara notificacion. Si el tipo requiere aprobacion (URGENCIA / ADICIONAL),
     * queda en estado PENDIENTE_APROBACION.
     */
    @Transactional
    public Solicitud crear(CrearSolicitudRequest req) {
        Cliente cliente = clienteRepo.findById(req.clienteId())
            .orElseThrow(() -> ApiException.notFound("Cliente", req.clienteId()));
        Usuario creador = usuarioRepo.findById(req.creadorId())
            .orElseThrow(() -> ApiException.notFound("Usuario", req.creadorId()));

        var resultado = clasificacion.clasificar(
            req.tipo(),
            cliente.getTipo(),
            req.prioridad(),
            req.unidadSugerida(),
            req.descripcion());

        UnidadProductivaEntity unidad = unidadRepo.findByNombre(resultado.unidad())
            .orElseThrow(() -> ApiException.badRequest("Unidad no existe: " + resultado.unidad()));

        EstadoSolicitud estado = resultado.tipo().requiereAprobacion()
            ? EstadoSolicitud.PENDIENTE_APROBACION
            : EstadoSolicitud.REGISTRADA;

        Solicitud s = new Solicitud(
            cliente, unidad, creador,
            resultado.tipo(), resultado.prioridad(),
            req.descripcion(), estado, req.plazo());

        s = repo.save(s);
        log.info("Solicitud creada id={} tipo={} unidad={} estado={}",
            s.getId(), s.getTipo(), unidad.getNombre(), estado);

        notificaciones.notificarCreacion(s);
        return s;
    }

    /**
     * Reclasifica una solicitud (cambio de tipo y/o unidad).
     * Solo accesible por roles autorizados (validacion en el controller via @PreAuthorize).
     */
    @Transactional
    public Solicitud reclasificar(Long id, ReclasificarRequest req) {
        Solicitud s = repo.findById(id).orElseThrow(() -> ApiException.notFound("Solicitud", id));
        UnidadProductivaEntity nuevaUnidad = unidadRepo.findByNombre(req.nuevaUnidad())
            .orElseThrow(() -> ApiException.badRequest("Unidad no existe: " + req.nuevaUnidad()));
        s.setTipo(req.nuevoTipo());
        s.setUnidad(nuevaUnidad);
        Solicitud actualizada = repo.save(s);
        notificaciones.notificarReclasificacion(actualizada, req.motivo());
        return actualizada;
    }

    @Transactional
    public Solicitud aprobar(Long id) {
        Solicitud s = repo.findById(id).orElseThrow(() -> ApiException.notFound("Solicitud", id));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE_APROBACION) {
            throw ApiException.badRequest("La solicitud no esta pendiente de aprobacion");
        }
        s.setEstado(EstadoSolicitud.APROBADA);
        return repo.save(s);
    }

    @Transactional
    public Solicitud rechazar(Long id) {
        Solicitud s = repo.findById(id).orElseThrow(() -> ApiException.notFound("Solicitud", id));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE_APROBACION) {
            throw ApiException.badRequest("La solicitud no esta pendiente de aprobacion");
        }
        s.setEstado(EstadoSolicitud.RECHAZADA);
        return repo.save(s);
    }

    @Transactional(readOnly = true)
    public Solicitud porId(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("Solicitud", id));
    }

    @Transactional(readOnly = true)
    public List<Solicitud> buscar(Instant desde, Instant hasta,
                                   UnidadProductiva unidad, TipoSolicitud tipo) {
        return repo.buscar(desde, hasta, unidad, tipo);
    }
}
