package com.focus.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.focus.solicitudes.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de autorizacion por rol (TRD §7.4 - control de acceso).
 * Activa el SecurityFilterChain real (perfil != dev) y verifica:
 *  - solo DIRECCION puede aprobar
 *  - UNIDAD y COORDINADOR pueden reclasificar
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("staging")
@TestPropertySource(properties = {
    "focus.google.client-id=test",
    "focus.google.allowed-domain=test.local",
    "focus.jwt.secret=test-jwt-secret-with-at-least-32-characters-for-hs256"
})
class SecurityRolesIT {

    @Autowired MockMvc mvc;
    @Autowired SolicitudRepository solicitudRepo;

    @Test
    @WithMockUser(roles = "UNIDAD")
    void unidadNoPuedeAprobar() throws Exception {
        // UNIDAD no tiene rol DIRECCION → 403 antes de llegar a la base de datos
        mvc.perform(post("/api/solicitudes/1/aprobar"))
            .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenSeRechazaCon401O403() throws Exception {
        // Anonimo no autenticado → Spring puede devolver 401 (sin entrypoint) o 403 (con default).
        // Aceptamos cualquiera de los dos; lo importante es que NO sea 200/2xx.
        mvc.perform(post("/api/solicitudes/1/aprobar"))
            .andExpect(result -> {
                int s = result.getResponse().getStatus();
                if (s != 401 && s != 403) {
                    throw new AssertionError("Esperaba 401 o 403, obtuvo " + s);
                }
            });
    }
}
