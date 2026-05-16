package com.focus.tareas;

import com.focus.common.EstadoTarea;
import com.focus.solicitudes.Solicitud;
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
@Table(name = "tareas")
@EntityListeners(AuditingEntityListener.class)
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitud_id")
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne
    @JoinColumn(name = "depende_de_id")
    private Tarea dependeDe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @Column(name = "tiempo_real_minutos")
    private Integer tiempoRealMinutos;

    @Column(name = "fecha_inicio")
    private Instant fechaInicio;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    public Tarea() { }

    public Tarea(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public Long getId() { return id; }
    public Solicitud getSolicitud() { return solicitud; }
    public Usuario getResponsable() { return responsable; }
    public Tarea getDependeDe() { return dependeDe; }
    public EstadoTarea getEstado() { return estado; }
    public Integer getTiempoRealMinutos() { return tiempoRealMinutos; }
    public Instant getFechaInicio() { return fechaInicio; }
    public Instant getFechaCierre() { return fechaCierre; }

    public void setResponsable(Usuario responsable) { this.responsable = responsable; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }
    public void setTiempoRealMinutos(Integer tiempo) { this.tiempoRealMinutos = tiempo; }
    public void setFechaInicio(Instant f) { this.fechaInicio = f; }
    public void setFechaCierre(Instant f) { this.fechaCierre = f; }
    public void setDependeDe(Tarea t) { this.dependeDe = t; }
}
