package com.focus.tareas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.focus.clientes.Cliente;
import com.focus.clientes.UnidadProductivaEntity;
import com.focus.common.ApiException;
import com.focus.common.EstadoSolicitud;
import com.focus.common.EstadoTarea;
import com.focus.common.Prioridad;
import com.focus.common.Rol;
import com.focus.common.TipoCliente;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.notificaciones.NotificacionService;
import com.focus.solicitudes.Solicitud;
import com.focus.solicitudes.SolicitudRepository;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock TareaRepository tareaRepo;
    @Mock HistorialEstadoRepository historialRepo;
    @Mock SolicitudRepository solicitudRepo;
    @Mock UsuarioRepository usuarioRepo;
    @Mock NotificacionService notificaciones;

    @InjectMocks TareaService service;

    Usuario user;
    Solicitud solicitudRegistrada;

    @BeforeEach
    void setup() {
        user = new Usuario("u@focus.co", "U", Rol.UNIDAD);
        Cliente cliente = new Cliente("Acme", TipoCliente.MENSUAL);
        UnidadProductivaEntity unidad = new UnidadProductivaEntity(UnidadProductiva.DISENO);
        solicitudRegistrada = new Solicitud(cliente, unidad, user,
            TipoSolicitud.MENSUAL, Prioridad.MEDIA, "x",
            EstadoSolicitud.REGISTRADA, null);
    }

    @Test
    void crearTareaDesdeSolicitudRegistrada() {
        when(solicitudRepo.findById(1L)).thenReturn(Optional.of(solicitudRegistrada));
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarea t = service.crearDesdeSolicitud(1L, null);

        assertThat(t.getEstado()).isEqualTo(EstadoTarea.PENDIENTE);
        assertThat(t.getSolicitud()).isSameAs(solicitudRegistrada);
    }

    @Test
    void noSePuedeCrearTareaDeSolicitudPendienteAprobacion() {
        solicitudRegistrada.setEstado(EstadoSolicitud.PENDIENTE_APROBACION);
        when(solicitudRepo.findById(1L)).thenReturn(Optional.of(solicitudRegistrada));

        assertThatThrownBy(() -> service.crearDesdeSolicitud(1L, null))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("PENDIENTE_APROBACION");
    }

    @Test
    void cambiarEstadoValidoFunciona() {
        Tarea t = new Tarea(solicitudRegistrada);
        when(tareaRepo.findById(1L)).thenReturn(Optional.of(t));
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarea actualizada = service.cambiarEstado(1L, EstadoTarea.EN_CURSO, user, "iniciando");

        assertThat(actualizada.getEstado()).isEqualTo(EstadoTarea.EN_CURSO);
        assertThat(actualizada.getFechaInicio()).isNotNull();
        verify(historialRepo, times(1)).save(any());
        verify(notificaciones, times(1)).notificarCambioEstado(actualizada);
    }

    @Test
    void cambiarEstadoInvalidoFalla() {
        Tarea t = new Tarea(solicitudRegistrada);
        when(tareaRepo.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.cambiarEstado(1L, EstadoTarea.COMPLETADA, user, null))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Transicion invalida");
    }

    @Test
    void cerrarRequiereTiempoYEstadoEnCurso() {
        Tarea t = new Tarea(solicitudRegistrada);
        t.setEstado(EstadoTarea.EN_CURSO);
        when(tareaRepo.findById(1L)).thenReturn(Optional.of(t));
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarea cerrada = service.cerrar(1L, 120, user);

        assertThat(cerrada.getEstado()).isEqualTo(EstadoTarea.COMPLETADA);
        assertThat(cerrada.getTiempoRealMinutos()).isEqualTo(120);
        assertThat(cerrada.getFechaCierre()).isNotNull();
    }

    @Test
    void cerrarSinTiempoLanza400() {
        Tarea t = new Tarea(solicitudRegistrada);
        t.setEstado(EstadoTarea.EN_CURSO);
        when(tareaRepo.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.cerrar(1L, 0, user))
            .isInstanceOf(ApiException.class);
    }

    @Test
    void cerrarTareaQueNoEsTuya_unidadFalla() {
        // Checklist seguridad #3: ownership. Un usuario UNIDAD que NO es el responsable
        // no puede cerrar la tarea de otro.
        Usuario otroDuenno = new Usuario("otro@focus.co", "Otro", Rol.UNIDAD);
        Tarea t = new Tarea(solicitudRegistrada);
        t.setResponsable(otroDuenno);
        t.setEstado(EstadoTarea.EN_CURSO);
        when(tareaRepo.findById(1L)).thenReturn(Optional.of(t));

        Usuario otroUsuario = new Usuario("intruso@focus.co", "Intruso", Rol.UNIDAD);
        assertThatThrownBy(() -> service.cerrar(1L, 30, otroUsuario))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("ownership");
    }

    @Test
    void cerrarTareaPendienteFalla() {
        Tarea t = new Tarea(solicitudRegistrada);
        when(tareaRepo.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.cerrar(1L, 30, user))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("EN_CURSO");
    }
}
