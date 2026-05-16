package com.focus.notificaciones;

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
@Table(name = "notificaciones")
public class Notificacion {

    public enum Estado { PENDIENTE, ENVIADA, FALLIDA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String destinatario;

    @Column(nullable = false, length = 240)
    private String asunto;

    @Column(nullable = false, length = 4000)
    private String cuerpo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Estado estado = Estado.PENDIENTE;

    @Column(nullable = false)
    private int intentos = 0;

    @Column(name = "error_mensaje", length = 2000)
    private String errorMensaje;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    public Notificacion() { }

    public Notificacion(String destinatario, String asunto, String cuerpo) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
    }

    public Long getId() { return id; }
    public String getDestinatario() { return destinatario; }
    public String getAsunto() { return asunto; }
    public String getCuerpo() { return cuerpo; }
    public Estado getEstado() { return estado; }
    public int getIntentos() { return intentos; }

    public void marcarEnviada() {
        this.estado = Estado.ENVIADA;
        this.fechaEnvio = Instant.now();
    }

    public void marcarFallida(String error) {
        this.estado = Estado.FALLIDA;
        this.errorMensaje = error;
    }

    public void incrementarIntentos() { this.intentos++; }
}
