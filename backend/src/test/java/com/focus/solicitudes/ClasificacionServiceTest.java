package com.focus.solicitudes;

import static org.assertj.core.api.Assertions.assertThat;

import com.focus.common.Prioridad;
import com.focus.common.TipoCliente;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios de las reglas de clasificacion (Design Doc §8 / TRD §6).
 * Prueban comportamiento, no detalles internos.
 */
class ClasificacionServiceTest {

    private final ClasificacionService svc = new ClasificacionService();

    @Nested
    @DisplayName("Resolucion de tipo")
    class ResolucionDeTipo {

        @Test
        @DisplayName("Tipo MENSUAL se mantiene si el cliente es MENSUAL")
        void mensualConClienteMensualSeMantiene() {
            var r = svc.clasificar(TipoSolicitud.MENSUAL, TipoCliente.MENSUAL,
                Prioridad.MEDIA, UnidadProductiva.DISENO, "diseño de banner");
            assertThat(r.tipo()).isEqualTo(TipoSolicitud.MENSUAL);
        }

        @Test
        @DisplayName("Tipo MENSUAL se corrige a PUNTUAL si el cliente NO es mensual")
        void mensualConClienteNoMensualSeCorrige() {
            var r = svc.clasificar(TipoSolicitud.MENSUAL, TipoCliente.PUNTUAL,
                Prioridad.MEDIA, UnidadProductiva.DISENO, "branding");
            assertThat(r.tipo()).isEqualTo(TipoSolicitud.PUNTUAL);
        }

        @Test
        @DisplayName("Tipo URGENCIA se mantiene independiente del cliente")
        void urgenciaSeMantiene() {
            var r = svc.clasificar(TipoSolicitud.URGENCIA, TipoCliente.OCASIONAL,
                Prioridad.MEDIA, UnidadProductiva.SOCIAL_MEDIA, "publicar ya");
            assertThat(r.tipo()).isEqualTo(TipoSolicitud.URGENCIA);
        }
    }

    @Nested
    @DisplayName("Resolucion de prioridad")
    class ResolucionDePrioridad {

        @Test
        @DisplayName("URGENCIA con prioridad BAJA escala a CRITICA")
        void urgenciaEscala() {
            var r = svc.clasificar(TipoSolicitud.URGENCIA, TipoCliente.MENSUAL,
                Prioridad.BAJA, UnidadProductiva.DISENO, "x");
            assertThat(r.prioridad()).isEqualTo(Prioridad.CRITICA);
        }

        @Test
        @DisplayName("URGENCIA con prioridad ALTA se respeta")
        void urgenciaAltaSeRespeta() {
            var r = svc.clasificar(TipoSolicitud.URGENCIA, TipoCliente.MENSUAL,
                Prioridad.ALTA, UnidadProductiva.DISENO, "x");
            assertThat(r.prioridad()).isEqualTo(Prioridad.ALTA);
        }

        @Test
        @DisplayName("Prioridad MEDIA en tipo MENSUAL no se altera")
        void mensualMediaSeRespeta() {
            var r = svc.clasificar(TipoSolicitud.MENSUAL, TipoCliente.MENSUAL,
                Prioridad.MEDIA, UnidadProductiva.DISENO, "x");
            assertThat(r.prioridad()).isEqualTo(Prioridad.MEDIA);
        }
    }

    @Nested
    @DisplayName("Inferencia de unidad por descripcion")
    class InferenciaDeUnidad {

        @Test
        @DisplayName("Si se sugiere unidad explicita, se respeta")
        void unidadExplicita() {
            var r = svc.clasificar(TipoSolicitud.MENSUAL, TipoCliente.MENSUAL,
                Prioridad.MEDIA, UnidadProductiva.TRAFFICKER, "post para instagram");
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.TRAFFICKER);
        }

        @Test
        @DisplayName("Descripcion con 'video' se infiere VIDEO")
        void inferirVideo() {
            var r = svc.clasificar(TipoSolicitud.PUNTUAL, TipoCliente.PUNTUAL,
                Prioridad.MEDIA, null, "Editar video corporativo");
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.VIDEO);
        }

        @Test
        @DisplayName("Descripcion con 'logo' se infiere DISENO")
        void inferirDiseno() {
            var r = svc.clasificar(TipoSolicitud.PUNTUAL, TipoCliente.PUNTUAL,
                Prioridad.MEDIA, null, "Necesito un logo para marca nueva");
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.DISENO);
        }

        @Test
        @DisplayName("Descripcion con 'instagram' se infiere SOCIAL_MEDIA")
        void inferirSocialMedia() {
            var r = svc.clasificar(TipoSolicitud.MENSUAL, TipoCliente.MENSUAL,
                Prioridad.MEDIA, null, "reel para instagram");
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.SOCIAL_MEDIA);
        }

        @Test
        @DisplayName("Descripcion con 'ads' se infiere TRAFFICKER")
        void inferirTrafficker() {
            var r = svc.clasificar(TipoSolicitud.MENSUAL, TipoCliente.MENSUAL,
                Prioridad.MEDIA, null, "Pauta ads en facebook");
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.TRAFFICKER);
        }

        @Test
        @DisplayName("Descripcion ambigua → ADMINISTRATIVO como default")
        void inferenciaAmbigua() {
            var r = svc.clasificar(TipoSolicitud.PUNTUAL, TipoCliente.PUNTUAL,
                Prioridad.MEDIA, null, "asunto sin palabras clave");
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.ADMINISTRATIVO);
        }

        @Test
        @DisplayName("Descripcion null no rompe la inferencia")
        void descripcionNull() {
            var r = svc.clasificar(TipoSolicitud.PUNTUAL, TipoCliente.PUNTUAL,
                Prioridad.MEDIA, null, null);
            assertThat(r.unidad()).isEqualTo(UnidadProductiva.ADMINISTRATIVO);
        }
    }
}
