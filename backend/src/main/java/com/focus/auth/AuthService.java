package com.focus.auth;

import com.focus.common.ApiException;
import com.focus.common.Rol;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final GoogleTokenValidator googleValidator;
    private final UsuarioRepository usuarioRepo;
    private final JwtService jwtService;

    public AuthService(GoogleTokenValidator googleValidator,
                       UsuarioRepository usuarioRepo,
                       JwtService jwtService) {
        this.googleValidator = googleValidator;
        this.usuarioRepo = usuarioRepo;
        this.jwtService = jwtService;
    }

    /**
     * Login con Google: valida el ID token, crea/encuentra el usuario y emite JWT propio.
     * Reintenta hasta 3 veces ante fallos transitorios de Google (TRD §7.3).
     */
    @Retryable(retryFor = { java.io.IOException.class },
               maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    @Transactional
    public AuthResponse loginConGoogle(String googleIdToken) {
        Payload payload = googleValidator.validar(googleIdToken);
        String email = payload.getEmail();
        String nombre = (String) payload.get("name");
        if (email == null) {
            throw ApiException.forbidden("Token sin email");
        }

        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseGet(() -> {
                log.info("Creando nuevo usuario: {}", email);
                return usuarioRepo.save(new Usuario(email, nombre != null ? nombre : email, Rol.UNIDAD));
            });

        if (!usuario.isActivo()) {
            throw ApiException.forbidden("Usuario desactivado");
        }

        String jwt = jwtService.generar(usuario.getEmail(), usuario.getRol());
        return new AuthResponse(jwt, usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }
}
