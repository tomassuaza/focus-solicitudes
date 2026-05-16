package com.focus.clientes;

import com.focus.common.TipoCliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoCliente tipo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    public Cliente() { }

    public Cliente(String nombre, TipoCliente tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoCliente getTipo() { return tipo; }
    public boolean isActivo() { return activo; }

    public void setActivo(boolean activo) { this.activo = activo; }
}
