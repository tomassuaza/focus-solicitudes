package com.focus.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.focus.clientes.Cliente;
import com.focus.clientes.ClienteRepository;
import com.focus.common.EstadoSolicitud;
import com.focus.common.EstadoTarea;
import com.focus.common.Prioridad;
import com.focus.common.TipoCliente;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.solicitudes.CrearSolicitudRequest;
import com.focus.solicitudes.Solicitud;
import com.focus.solicitudes.SolicitudService;
import com.focus.tareas.Tarea;
import com.focus.tareas.TareaService;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test de integracion extremo a extremo (TRD §13).
 * Cubre: registro -> clasificacion -> creacion de tarea -> cambio estado -> cierre con tiempo.
 *
 * Usa H2 in-memory + flyway real; sin mocks de servicios internos. Solo mockea email vía TestConfig.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SolicitudFlujoCompletoIT {

    @Autowired SolicitudService solicitudService;
    @Autowired TareaService tareaService;
    @Autowired ClienteRepository clienteRepo;
    @Autowired UsuarioRepository usuarioRepo;

    @Test
    void flujoCompleto_registrar_clasificar_crearTarea_cerrar() {
        // Arrange: usar seed
        Cliente cliente = clienteRepo.findAll().stream()
            .filter(c -> c.getTipo() == TipoCliente.MENSUAL)
            .findFirst().orElseThrow();
        Usuario coord = usuarioRepo.findByEmail("coordinador@focusagency.co").orElseThrow();

        // 1. Registro de solicitud mensual
        var req = new CrearSolicitudRequest(
            cliente.getId(), coord.getId(),
            TipoSolicitud.MENSUAL, Prioridad.MEDIA,
            "Diseño de banner mensual para campaña",
            UnidadProductiva.DISENO, null);

        Solicitud s = solicitudService.crear(req);
        assertThat(s.getId()).isNotNull();
        assertThat(s.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(s.getUnidad().getNombre()).isEqualTo(UnidadProductiva.DISENO);

        // 2. Crear tarea desde la solicitud
        Tarea t = tareaService.crearDesdeSolicitud(s.getId(), null);
        assertThat(t.getEstado()).isEqualTo(EstadoTarea.PENDIENTE);

        // 3. Iniciar tarea
        t = tareaService.cambiarEstado(t.getId(), EstadoTarea.EN_CURSO, coord, "iniciando");
        assertThat(t.getEstado()).isEqualTo(EstadoTarea.EN_CURSO);
        assertThat(t.getFechaInicio()).isNotNull();

        // 4. Cerrar tarea con tiempo real
        t = tareaService.cerrar(t.getId(), 90, coord);
        assertThat(t.getEstado()).isEqualTo(EstadoTarea.COMPLETADA);
        assertThat(t.getTiempoRealMinutos()).isEqualTo(90);
        assertThat(t.getFechaCierre()).isNotNull();
    }

    @Test
    void urgenciaQuedaPendienteAprobacion() {
        Cliente cliente = clienteRepo.findAll().get(0);
        Usuario coord = usuarioRepo.findByEmail("coordinador@focusagency.co").orElseThrow();

        var req = new CrearSolicitudRequest(
            cliente.getId(), coord.getId(),
            TipoSolicitud.URGENCIA, Prioridad.BAJA,
            "Publicar reel ya en instagram",
            UnidadProductiva.SOCIAL_MEDIA, null);

        Solicitud s = solicitudService.crear(req);
        assertThat(s.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE_APROBACION);
        assertThat(s.getPrioridad()).isEqualTo(Prioridad.CRITICA);

        // Director aprueba
        Solicitud aprobada = solicitudService.aprobar(s.getId());
        assertThat(aprobada.getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
    }
}
