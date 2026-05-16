package com.focus.solicitudes;

import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody CrearSolicitudRequest req) {
        var s = service.crear(req);
        return ResponseEntity.status(201).body(SolicitudResponse.de(s));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.de(service.porId(id)));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponse>> listar(
        @RequestParam(required = false) Instant desde,
        @RequestParam(required = false) Instant hasta,
        @RequestParam(required = false) UnidadProductiva unidad,
        @RequestParam(required = false) TipoSolicitud tipo) {
        return ResponseEntity.ok(service.buscar(desde, hasta, unidad, tipo).stream()
            .map(SolicitudResponse::de).toList());
    }

    @PutMapping("/{id}/reclasificar")
    @PreAuthorize("hasAnyRole('COORDINADOR','UNIDAD')")
    public ResponseEntity<SolicitudResponse> reclasificar(
        @PathVariable Long id, @Valid @RequestBody ReclasificarRequest req) {
        return ResponseEntity.ok(SolicitudResponse.de(service.reclasificar(id, req)));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('DIRECCION')")
    public ResponseEntity<SolicitudResponse> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.de(service.aprobar(id)));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('DIRECCION')")
    public ResponseEntity<SolicitudResponse> rechazar(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.de(service.rechazar(id)));
    }
}
