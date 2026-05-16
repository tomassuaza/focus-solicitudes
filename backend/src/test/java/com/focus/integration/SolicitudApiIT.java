package com.focus.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focus.clientes.ClienteRepository;
import com.focus.common.Prioridad;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import com.focus.solicitudes.CrearSolicitudRequest;
import com.focus.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de integracion vía API REST con MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SolicitudApiIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ClienteRepository clienteRepo;
    @Autowired UsuarioRepository usuarioRepo;

    @Test
    void postSolicitudDevuelve201YBody() throws Exception {
        var cliente = clienteRepo.findAll().get(0);
        var creador = usuarioRepo.findAll().get(0);

        var req = new CrearSolicitudRequest(
            cliente.getId(), creador.getId(),
            TipoSolicitud.MENSUAL, Prioridad.MEDIA,
            "Pieza de diseño", UnidadProductiva.DISENO, null);

        mvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("MENSUAL"))
            .andExpect(jsonPath("$.unidad").value("DISENO"))
            .andExpect(jsonPath("$.estado").value("REGISTRADA"));
    }

    @Test
    void postSinCamposObligatoriosDevuelve400() throws Exception {
        String body = "{\"clienteId\":null}";
        mvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSolicitudInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/solicitudes/999999"))
            .andExpect(status().isNotFound());
    }
}
