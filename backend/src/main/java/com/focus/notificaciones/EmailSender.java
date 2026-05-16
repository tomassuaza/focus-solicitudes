package com.focus.notificaciones;

/**
 * Abstraccion sobre el envio de correo. Permite stub en tests sin SMTP real.
 */
public interface EmailSender {
    void enviar(String destinatario, String asunto, String cuerpo);
}
