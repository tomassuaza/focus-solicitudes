package com.focus.solicitudes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.focus.clientes.Cliente;
import com.focus.clientes.ClienteRepository;
import com.focus.clientes.UnidadProductivaEntity;
import com.focus.clientes.UnidadProductivaRepository;
import com.focus.common.ApiException;
import com.focus.common.EstadoSolicitud;
import com.focus.common.Prioridad;
import com.focus.common.Rol;
import com.focus.common.TipoCliente;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.notificaciones.NotificacionService;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock SolicitudRepository repo;
    @Mock ClienteRepository clienteRepo;
    @Mock UsuarioRepository usuarioRepo;
    @Mock UnidadProductivaRepository unidadRepo;
    @Mock NotificacionService notificaciones;

    SolicitudService service;

    Cliente clienteMensual;
    Usuario creador;
    UnidadProductivaEntity unidadDiseno;

    @BeforeEach
    void setup() {
        ClasificacionService clasif = new ClasificacionService();
        service = new SolicitudService(repo, clienteRepo, usuarioRepo, unidadRepo, clasif, notificaciones);

        clienteMensual = new Cliente("Acme", TipoCliente.MENSUAL);
        creador = new Usuario("user@focus.co", "User", Rol.COORDINADOR);
        unidadDiseno = new UnidadProductivaEntity(UnidadProductiva.DISENO);
    }

    @Test
    void crearSolicitudMensualQuedaRegistrada() {
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(clienteMensual));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(creador));
        when(unidadRepo.findByNombre(UnidadProductiva.DISENO)).thenReturn(Optional.of(unidadDiseno));
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new CrearSolicitudRequest(1L, 2L, TipoSolicitud.MENSUAL,
            Prioridad.MEDIA, "Banner del mes", UnidadProductiva.DISENO, null);

        Solicitud s = service.crear(req);

        assertThat(s.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
        assertThat(s.getTipo()).isEqualTo(TipoSolicitud.MENSUAL);
        verify(notificaciones, times(1)).notificarCreacion(s);
    }

    @Test
    void crearSolicitudUrgenciaQuedaPendienteAprobacion() {
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(clienteMensual));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(creador));
        when(unidadRepo.findByNombre(UnidadProductiva.SOCIAL_MEDIA))
            .thenReturn(Optional.of(new UnidadProductivaEntity(UnidadProductiva.SOCIAL_MEDIA)));
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new CrearSolicitudRequest(1L, 2L, TipoSolicitud.URGENCIA,
            Prioridad.BAJA, "publicar ya en instagram", UnidadProductiva.SOCIAL_MEDIA, null);

        Solicitud s = service.crear(req);

        assertThat(s.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE_APROBACION);
        assertThat(s.getPrioridad()).isEqualTo(Prioridad.CRITICA);
    }

    @Test
    void crearConClienteInexistenteLanza404() {
        when(clienteRepo.findById(99L)).thenReturn(Optional.empty());
        var req = new CrearSolicitudRequest(99L, 2L, TipoSolicitud.MENSUAL,
            Prioridad.MEDIA, "x", UnidadProductiva.DISENO, null);

        assertThatThrownBy(() -> service.crear(req))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Cliente");
    }

    @Test
    void aprobarSoloFuncionaSiEstaPendiente() {
        Solicitud s = new Solicitud(clienteMensual, unidadDiseno, creador,
            TipoSolicitud.URGENCIA, Prioridad.CRITICA, "x",
            EstadoSolicitud.PENDIENTE_APROBACION, null);
        when(repo.findById(1L)).thenReturn(Optional.of(s));
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        Solicitud aprobada = service.aprobar(1L);

        assertThat(aprobada.getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
    }

    @Test
    void aprobarSolicitudYaRegistradaFalla() {
        Solicitud s = new Solicitud(clienteMensual, unidadDiseno, creador,
            TipoSolicitud.MENSUAL, Prioridad.MEDIA, "x",
            EstadoSolicitud.REGISTRADA, null);
        when(repo.findById(1L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> service.aprobar(1L))
            .isInstanceOf(ApiException.class);
    }

    @Test
    void reclasificarCambiaTipoYUnidadYNotifica() {
        Solicitud s = new Solicitud(clienteMensual, unidadDiseno, creador,
            TipoSolicitud.MENSUAL, Prioridad.MEDIA, "x",
            EstadoSolicitud.REGISTRADA, null);
        when(repo.findById(1L)).thenReturn(Optional.of(s));
        UnidadProductivaEntity video = new UnidadProductivaEntity(UnidadProductiva.VIDEO);
        when(unidadRepo.findByNombre(UnidadProductiva.VIDEO)).thenReturn(Optional.of(video));
        when(repo.save(any(Solicitud.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ReclasificarRequest(TipoSolicitud.PROYECTO_ESPECIAL,
            UnidadProductiva.VIDEO, "es un video, no diseño");
        Solicitud actualizada = service.reclasificar(1L, req);

        assertThat(actualizada.getTipo()).isEqualTo(TipoSolicitud.PROYECTO_ESPECIAL);
        assertThat(actualizada.getUnidad().getNombre()).isEqualTo(UnidadProductiva.VIDEO);
        verify(notificaciones).notificarReclasificacion(Mockito.eq(actualizada), Mockito.anyString());
    }
}
