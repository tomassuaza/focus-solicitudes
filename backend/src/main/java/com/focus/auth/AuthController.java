package com.focus.auth;

import com.focus.common.Rol;
import com.focus.usuarios.Usuario;
import com.focus.usuarios.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepo;
    private final LoginRateLimiter rateLimiter;
    private final boolean demoEnabled;

    public AuthController(AuthService authService,
                           JwtService jwtService,
                           UsuarioRepository usuarioRepo,
                           LoginRateLimiter rateLimiter,
                           @Value("${focus.demo-enabled:true}") boolean demoEnabled) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioRepo = usuarioRepo;
        this.rateLimiter = rateLimiter;
        this.demoEnabled = demoEnabled;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginGoogle(
        @Valid @RequestBody LoginRequest req,
        jakarta.servlet.http.HttpServletRequest http) {
        // Checklist seguridad #2: limitar intentos de login por IP.
        rateLimiter.registrarIntento(http.getRemoteAddr());
        return ResponseEntity.ok(authService.loginConGoogle(req.idToken()));
    }

    /**
     * Login demo SIN Google OAuth. Util para entorno piloto y sustentacion.
     * Genera un JWT real para los usuarios seed (coordinador / disenador / director).
     * Solo activo si focus.demo-enabled=true (controlado por variable de entorno).
     */
    @GetMapping("/demo")
    public ResponseEntity<AuthResponse> loginDemo(
        @RequestParam(defaultValue = "COORDINADOR") String rol) {
        if (!demoEnabled) {
            return ResponseEntity.status(403).build();
        }
        Rol rolEnum;
        try {
            rolEnum = Rol.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException e) {
            rolEnum = Rol.COORDINADOR;
        }

        String email = switch (rolEnum) {
            case COORDINADOR -> "coordinador@focusagency.co";
            case UNIDAD -> "disenador@focusagency.co";
            case DIRECCION -> "director@focusagency.co";
        };

        Usuario u = usuarioRepo.findByEmail(email).orElseThrow();
        String token = jwtService.generar(u.getEmail(), u.getRol());
        return ResponseEntity.ok(new AuthResponse(token, u.getEmail(), u.getNombre(), u.getRol().name()));
    }
}
