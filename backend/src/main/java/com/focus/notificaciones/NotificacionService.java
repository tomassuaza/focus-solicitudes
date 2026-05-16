package com.focus.notificaciones;

import com.focus.solicitudes.Solicitud;
import com.focus.tareas.Tarea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Envia notificaciones por correo de forma asincrona, con reintentos controlados
 * (TRD §7.3, §7.4, §12 - politica de retry para SMTP/OAuth).
 *
 * Cada notificacion se persiste antes del envio para garantizar trazabilidad
 * incluso si el SMTP cae.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository repo;
    private final EmailSender sender;

    public NotificacionService(NotificacionRepository repo, EmailSender sender) {
        this.repo = repo;
        this.sender = sender;
    }

    @Async
    public void notificarCreacion(Solicitud s) {
        String asunto = "[Focus] Nueva solicitud #" + s.getId();
        String cuerpo = String.format(
            "Se registro una nueva solicitud:%n%nCliente: %s%nTipo: %s%nPrioridad: %s%nUnidad: %s%n%nDescripcion:%n%s",
            s.getCliente().getNombre(),
            s.getTipo(),
            s.getPrioridad(),
            s.getUnidad().getNombre(),
            s.getDescripcion());
        encolarYEnviar(s.getCreador().getEmail(), asunto, cuerpo);
    }

    @Async
    public void notificarReclasificacion(Solicitud s, String motivo) {
        String asunto = "[Focus] Solicitud #" + s.getId() + " reclasificada";
        String cuerpo = String.format(
            "La solicitud fue reclasificada.%n%nNuevo tipo: %s%nNueva unidad: %s%nMotivo: %s",
            s.getTipo(), s.getUnidad().getNombre(), motivo != null ? motivo : "(sin motivo)");
        encolarYEnviar(s.getCreador().getEmail(), asunto, cuerpo);
    }

    @Async
    public void notificarCambioEstado(Tarea t) {
        if (t.getResponsable() == null) {
            return;
        }
        String asunto = "[Focus] Tarea #" + t.getId() + " - " + t.getEstado();
        String cuerpo = String.format(
            "La tarea cambio de estado.%n%nEstado actual: %s%nCliente: %s",
            t.getEstado(), t.getSolicitud().getCliente().getNombre());
        encolarYEnviar(t.getResponsable().getEmail(), asunto, cuerpo);
    }

    @Transactional
    void encolarYEnviar(String destinatario, String asunto, String cuerpo) {
        Notificacion n = repo.save(new Notificacion(destinatario, asunto, cuerpo));
        try {
            enviarConRetry(n.getId(), destinatario, asunto, cuerpo);
            n.marcarEnviada();
            repo.save(n);
        } catch (Exception e) {
            log.warn("Notificacion {} fallo definitivamente: {}", n.getId(), e.getMessage());
            n.marcarFallida(e.getMessage());
            repo.save(n);
        }
    }

    @Retryable(retryFor = { MailException.class, DataAccessException.class },
               maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public void enviarConRetry(Long id, String destinatario, String asunto, String cuerpo) {
        log.debug("Enviando notificacion {} a {}", id, destinatario);
        sender.enviar(destinatario, asunto, cuerpo);
    }

    @Recover
    public void recuperar(MailException ex, Long id, String destinatario, String asunto, String cuerpo) {
        log.error("Notificacion {} agotada despues de 3 intentos: {}", id, ex.getMessage());
        throw ex;
    }
}
