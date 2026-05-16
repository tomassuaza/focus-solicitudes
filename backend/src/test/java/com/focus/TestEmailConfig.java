package com.focus;

import com.focus.notificaciones.EmailSender;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub global de EmailSender en perfil "test".
 * Se registra como bean Spring para sustituir SmtpEmailSender (que esta @Profile("!test")).
 */
@Component
@Profile("test")
public class TestEmailConfig implements EmailSender {

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        // no-op: en tests no se envia correo real
    }
}
