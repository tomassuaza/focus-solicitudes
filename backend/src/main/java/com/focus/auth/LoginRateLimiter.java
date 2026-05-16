package com.focus.auth;

import com.focus.common.ApiException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Rate limiter simple en memoria para endpoints de autenticacion.
 *
 * Politica: maximo 10 intentos por IP/email en ventana de 60 segundos.
 * Si se excede, rechaza con 429 Too Many Requests.
 *
 * Cumple checklist de seguridad #2: login limita intentos.
 *
 * Limitacion conocida: el estado vive en memoria del nodo. En un cluster real
 * (Redis, etc.) deberia compartirse. Para el piloto en un solo nodo es suficiente.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, IntentoVentana> intentos = new ConcurrentHashMap<>();

    public void registrarIntento(String clave) {
        if (clave == null || clave.isBlank()) {
            return;
        }
        Instant ahora = Instant.now();
        intentos.compute(clave, (k, v) -> {
            if (v == null || ahora.getEpochSecond() - v.inicio > WINDOW_SECONDS) {
                return new IntentoVentana(ahora.getEpochSecond(), 1);
            }
            v.cuenta++;
            return v;
        });
        IntentoVentana actual = intentos.get(clave);
        if (actual != null && actual.cuenta > MAX_ATTEMPTS) {
            throw new ApiException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos de login. Intenta en " + WINDOW_SECONDS + " segundos.");
        }
    }

    private static class IntentoVentana {
        long inicio;
        int cuenta;
        IntentoVentana(long inicio, int cuenta) {
            this.inicio = inicio;
            this.cuenta = cuenta;
        }
    }
}
