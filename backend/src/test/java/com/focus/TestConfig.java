package com.focus;

import com.focus.notificaciones.EmailSender;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Configuracion adicional opcional para tests. Para el stub global de EmailSender
 * que se carga automaticamente con classpath scanning, ver {@link TestEmailConfig}.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    public static class StubEmailSender implements EmailSender {
        public final List<String> enviados = new ArrayList<>();
        @Override
        public void enviar(String destinatario, String asunto, String cuerpo) {
            enviados.add(destinatario + "|" + asunto);
        }
    }

    @Bean
    @Primary
    public EmailSender stubEmailSender() {
        return new StubEmailSender();
    }
}
