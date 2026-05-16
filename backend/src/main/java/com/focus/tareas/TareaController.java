package com.focus.tareas;

import com.focus.common.EstadoTarea;
import com.focus.common.UnidadProductiva;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService service;
    private final UsuarioRepository usuarioRepo;

    public TareaController(TareaService service, UsuarioRepository usuarioRepo) {
        this.service = service;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public ResponseEntity<List<TareaResponse>> listar(
        @RequestParam(required = false) UnidadProductiva unidad,
        @RequestParam(required = false) EstadoTarea estado) {
        return ResponseEntity.ok(service.buscar(unidad, estado).stream()
            .map(TareaResponse::de).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(TareaResponse.de(service.porId(id)));
    }

    @PostMapping
    public ResponseEntity<TareaResponse> crear(
        @RequestParam Long solicitudId,
        @RequestParam(required = false) Long responsableId) {
        Tarea t = service.crearDesdeSolicitud(solicitudId, responsableId);
        return ResponseEntity.status(201).body(TareaResponse.de(t));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<TareaResponse> cambiarEstado(
        @PathVariable Long id, @Valid @RequestBody CambioEstadoRequest req,
        @AuthenticationPrincipal String emailUsuario) {
        Usuario u = usuarioActual(emailUsuario);
        return ResponseEntity.ok(TareaResponse.de(
            service.cambiarEstado(id, req.nuevoEstado(), u, req.motivo())));
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<TareaResponse> cerrar(
        @PathVariable Long id, @Valid @RequestBody CerrarTareaRequest req,
        @AuthenticationPrincipal String emailUsuario) {
        Usuario u = usuarioActual(emailUsuario);
        return ResponseEntity.ok(TareaResponse.de(
            service.cerrar(id, req.tiempoRealMinutos(), u)));
    }

    private Usuario usuarioActual(String email) {
        return usuarioRepo.findByEmail(email != null ? email : "coordinador@focusagency.co")
            .orElseGet(() -> usuarioRepo.findAll().get(0));
    }
}
