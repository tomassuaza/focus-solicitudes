package com.focus.tareas;

import com.focus.common.EstadoTarea;
import com.focus.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "historial_estado")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 40)
    private EstadoTarea estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 40)
    private EstadoTarea estadoNuevo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(length = 500)
    private String motivo;

    public HistorialEstado() { }

    public HistorialEstado(Tarea tarea, EstadoTarea anterior, EstadoTarea nuevo,
                            Usuario usuario, String motivo) {
        this.tarea = tarea;
        this.estadoAnterior = anterior;
        this.estadoNuevo = nuevo;
        this.usuario = usuario;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public EstadoTarea getEstadoAnterior() { return estadoAnterior; }
    public EstadoTarea getEstadoNuevo() { return estadoNuevo; }
    public Instant getFecha() { return fecha; }
}
