package com.focus.auth;

import com.focus.common.ApiException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Valida ID tokens emitidos por Google (OAuth 2.0).
 * Verifica:
 *  - firma criptografica del token
 *  - audience (nuestro client_id)
 *  - dominio corporativo (claim 'hd')
 */
@Component
public class GoogleTokenValidator {

    private final GoogleIdTokenVerifier verifier;
    private final String dominioPermitido;

    public GoogleTokenValidator(
        @Value("${focus.google.client-id}") String clientId,
        @Value("${focus.google.allowed-domain}") String dominioPermitido) {
        this.dominioPermitido = dominioPermitido;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build();
    }

    public Payload validar(String idTokenString) {
        try {
            GoogleIdToken token = verifier.verify(idTokenString);
            if (token == null) {
                throw ApiException.forbidden("Token de Google invalido");
            }
            Payload payload = token.getPayload();
            String hd = (String) payload.get("hd");
            if (dominioPermitido != null && !dominioPermitido.isBlank()
                && !dominioPermitido.equalsIgnoreCase(hd)) {
                throw ApiException.forbidden("Dominio no autorizado: " + hd);
            }
            return payload;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.forbidden("No se pudo validar el token de Google");
        }
    }
}
