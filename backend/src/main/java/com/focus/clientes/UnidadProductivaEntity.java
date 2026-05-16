package com.focus.clientes;

import com.focus.common.UnidadProductiva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "unidades_productivas")
public class UnidadProductivaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60, unique = true)
    private UnidadProductiva nombre;

    @Column(nullable = false)
    private boolean activa = true;

    public UnidadProductivaEntity() { }

    public UnidadProductivaEntity(UnidadProductiva nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public UnidadProductiva getNombre() { return nombre; }
    public boolean isActiva() { return activa; }
}
