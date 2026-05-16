package com.focus.tareas;

import static org.assertj.core.api.Assertions.assertThat;

import com.focus.common.EstadoTarea;
import org.junit.jupiter.api.Test;

/**
 * Tests del invariante del ciclo de vida (Design Doc §8 / TRD §6).
 * El comportamiento es: PENDIENTE → EN_CURSO → COMPLETADA (o CANCELADA en cualquier punto previo a COMPLETADA).
 */
class EstadoTareaTransicionTest {

    @Test
    void pendientePuedeIrAEnCursoOCancelada() {
        assertThat(EstadoTarea.PENDIENTE.puedeTransicionarA(EstadoTarea.EN_CURSO)).isTrue();
        assertThat(EstadoTarea.PENDIENTE.puedeTransicionarA(EstadoTarea.CANCELADA)).isTrue();
        assertThat(EstadoTarea.PENDIENTE.puedeTransicionarA(EstadoTarea.COMPLETADA)).isFalse();
    }

    @Test
    void enCursoPuedeIrACompletadaOCancelada() {
        assertThat(EstadoTarea.EN_CURSO.puedeTransicionarA(EstadoTarea.COMPLETADA)).isTrue();
        assertThat(EstadoTarea.EN_CURSO.puedeTransicionarA(EstadoTarea.CANCELADA)).isTrue();
        assertThat(EstadoTarea.EN_CURSO.puedeTransicionarA(EstadoTarea.PENDIENTE)).isFalse();
    }

    @Test
    void completadaEsEstadoFinal() {
        for (EstadoTarea e : EstadoTarea.values()) {
            assertThat(EstadoTarea.COMPLETADA.puedeTransicionarA(e)).isFalse();
        }
    }

    @Test
    void canceladaEsEstadoFinal() {
        for (EstadoTarea e : EstadoTarea.values()) {
            assertThat(EstadoTarea.CANCELADA.puedeTransicionarA(e)).isFalse();
        }
    }
}
