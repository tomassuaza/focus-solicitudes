package com.focus.clientes;

import com.focus.common.ApiException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepo;

    public ClienteController(ClienteRepository clienteRepo) {
        this.clienteRepo = clienteRepo;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(
            clienteRepo.findAll().stream()
                .filter(Cliente::isActivo)
                .map(c -> new ClienteResponse(c.getId(), c.getNombre(), c.getTipo().name()))
                .toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COORDINADOR','DIRECCION')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody CrearClienteRequest req) {
        Cliente c = clienteRepo.save(new Cliente(req.nombre(), req.tipo()));
        return ResponseEntity.status(201)
            .body(new ClienteResponse(c.getId(), c.getNombre(), c.getTipo().name()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIRECCION')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        Cliente c = clienteRepo.findById(id)
            .orElseThrow(() -> ApiException.notFound("Cliente", id));
        c.setActivo(false);
        clienteRepo.save(c);
        return ResponseEntity.noContent().build();
    }

    public record ClienteResponse(Long id, String nombre, String tipo) { }
}
