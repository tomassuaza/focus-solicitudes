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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de carga concurrente (TRD §7.2 / NFR2: ≥ 20 usuarios concurrentes).
 *
 * Simula 20 hilos haciendo POST /solicitudes en paralelo y verifica:
 *  - todas exitosas (sin perdida)
 *  - tiempo total razonable
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentLoadTest {

    @Autowired SolicitudService service;
    @Autowired ClienteRepository clienteRepo;
    @Autowired UsuarioRepository usuarioRepo;

    @Test
    void soporta20UsuariosConcurrentes() throws Exception {
        var cliente = clienteRepo.findAll().get(0);
        var creador = usuarioRepo.findAll().get(0);

        int concurrencia = 20;
        ExecutorService pool = Executors.newFixedThreadPool(concurrencia);
        List<Callable<Long>> tareas = new ArrayList<>();

        for (int i = 0; i < concurrencia; i++) {
            final int idx = i;
            tareas.add(() -> {
                Instant t0 = Instant.now();
                service.crear(new CrearSolicitudRequest(
                    cliente.getId(), creador.getId(),
                    TipoSolicitud.MENSUAL, Prioridad.MEDIA,
                    "concurrente " + idx,
                    UnidadProductiva.DISENO, null));
                return Duration.between(t0, Instant.now()).toMillis();
            });
        }

        Instant t0 = Instant.now();
        List<Future<Long>> resultados = pool.invokeAll(tareas, 30, TimeUnit.SECONDS);
        long totalMs = Duration.between(t0, Instant.now()).toMillis();
        pool.shutdown();

        long completadas = resultados.stream().filter(Future::isDone).count();
        assertThat(completadas).isEqualTo(concurrencia);

        long maxMs = 0;
        for (Future<Long> r : resultados) {
            maxMs = Math.max(maxMs, r.get());
        }

        assertThat(maxMs)
            .as("Ningun request individual debe pasar de 2s incluso bajo concurrencia")
            .isLessThan(2000);
        assertThat(totalMs)
            .as("20 requests concurrentes deben completar en tiempo razonable")
            .isLessThan(10000);
    }
}
