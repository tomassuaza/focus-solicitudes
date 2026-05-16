package com.focus.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.focus.common.Rol;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwt = new JwtService(
        "test-jwt-secret-with-at-least-32-characters-for-hs256", 60);

    @Test
    void generaYValidaTokenConRol() {
        String token = jwt.generar("user@focus.co", Rol.COORDINADOR);
        Claims claims = jwt.validar(token);
        assertThat(claims.getSubject()).isEqualTo("user@focus.co");
        assertThat(claims.get("rol", String.class)).isEqualTo("COORDINADOR");
    }

    @Test
    void rechazaSecretCorto() {
        assertThatThrownBy(() -> new JwtService("corto", 60))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenManipuladoSeRechaza() {
        String token = jwt.generar("user@focus.co", Rol.UNIDAD);
        String manipulado = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwt.validar(manipulado))
            .isInstanceOf(Exception.class);
    }
}
