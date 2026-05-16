package com.focus.solicitudes;

import com.focus.clientes.Cliente;
import com.focus.clientes.UnidadProductivaEntity;
import com.focus.common.EstadoSolicitud;
import com.focus.common.Prioridad;
import com.focus.common.TipoSolicitud;
import com.focus.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "solicitudes")
@EntityListeners(AuditingEntityListener.class)
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_id")
    private UnidadProductivaEntity unidad;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoSolicitud estado;

    private Instant plazo;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    public Solicitud() { }

    public Solicitud(Cliente cliente, UnidadProductivaEntity unidad, Usuario creador,
                     TipoSolicitud tipo, Prioridad prioridad, String descripcion,
                     EstadoSolicitud estado, Instant plazo) {
        this.cliente = cliente;
        this.unidad = unidad;
        this.creador = creador;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.estado = estado;
        this.plazo = plazo;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public UnidadProductivaEntity getUnidad() { return unidad; }
    public Usuario getCreador() { return creador; }
    public TipoSolicitud getTipo() { return tipo; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getDescripcion() { return descripcion; }
    public EstadoSolicitud getEstado() { return estado; }
    public Instant getPlazo() { return plazo; }
    public Instant getFechaCreacion() { return fechaCreacion; }

    public void setTipo(TipoSolicitud tipo) { this.tipo = tipo; }
    public void setUnidad(UnidadProductivaEntity unidad) { this.unidad = unidad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
}
