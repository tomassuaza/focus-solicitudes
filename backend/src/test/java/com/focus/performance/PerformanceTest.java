package com.focus.performance;

import static org.assertj.core.api.Assertions.assertThat;

import com.focus.clientes.ClienteRepository;
import com.focus.common.Prioridad;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.solicitudes.CrearSolicitudRequest;
import com.focus.solicitudes.SolicitudService;
import com.focus.usuarios.UsuarioRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de performance (TRD §7.1 / §13).
 * Verifica que el registro de una solicitud cumple el NFR1: < 2 segundos.
 */
@SpringBootTest
@ActiveProfiles("test")
class PerformanceTest {

    @Autowired SolicitudService service;
    @Autowired ClienteRepository clienteRepo;
    @Autowired UsuarioRepository usuarioRepo;

    @Test
    void registroSolicitud_cumpleSLA_menorA2Segundos() {
        var cliente = clienteRepo.findAll().get(0);
        var creador = usuarioRepo.findAll().get(0);

        // Warmup
        for (int i = 0; i < 3; i++) {
            service.crear(req(cliente.getId(), creador.getId(), "warmup " + i));
        }

        Instant t0 = Instant.now();
        service.crear(req(cliente.getId(), creador.getId(), "medicion"));
        Duration elapsed = Duration.between(t0, Instant.now());

        assertThat(elapsed.toMillis())
            .as("NFR1: registro debe responder en menos de 2 segundos")
            .isLessThan(2000);
    }

    @Test
    void registroLote100Solicitudes_promedioBajoSLA() {
        var cliente = clienteRepo.findAll().get(0);
        var creador = usuarioRepo.findAll().get(0);

        // Warmup
        for (int i = 0; i < 5; i++) {
            service.crear(req(cliente.getId(), creador.getId(), "wu " + i));
        }

        int n = 100;
        Instant t0 = Instant.now();
        for (int i = 0; i < n; i++) {
            service.crear(req(cliente.getId(), creador.getId(), "lote " + i));
        }
        long totalMs = Duration.between(t0, Instant.now()).toMillis();
        long avgMs = totalMs / n;

        assertThat(avgMs)
            .as("Promedio por registro en lote debe ser muy inferior a 2s")
            .isLessThan(500);
    }

    private CrearSolicitudRequest req(Long clienteId, Long creadorId, String desc) {
        return new CrearSolicitudRequest(
            clienteId, creadorId,
            TipoSolicitud.MENSUAL, Prioridad.MEDIA,
            desc, UnidadProductiva.DISENO, null);
    }
}
