package com.focus.reportes;

import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECCION','COORDINADOR')")
    public ResponseEntity<ReporteService.ReporteResponse> generar(
        @RequestParam(required = false) Instant desde,
        @RequestParam(required = false) Instant hasta,
        @RequestParam(required = false) UnidadProductiva unidad,
        @RequestParam(required = false) TipoSolicitud tipo) {
        return ResponseEntity.ok(service.generar(desde, hasta, unidad, tipo));
    }
}
