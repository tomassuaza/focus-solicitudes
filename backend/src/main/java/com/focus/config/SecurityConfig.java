package com.focus.config;

import com.focus.auth.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${focus.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    @Profile("!dev")
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .cors(c -> c.configurationSource(corsSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reg -> reg
                .requestMatchers("/api/auth/**", "/actuator/health", "/h2-console/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(h -> h.frameOptions(f -> f.disable()));
        return http.build();
    }

    /**
     * En perfil dev se exponen todos los endpoints sin auth para facilitar pruebas locales,
     * PERO el JwtAuthenticationFilter sigue activo. Esto permite que cuando llega un JWT
     * valido (ej. del endpoint /api/auth/demo), el SecurityContext se rellene y
     * los @PreAuthorize de los endpoints (control por rol) funcionen correctamente.
     *
     * En staging/prod usa el otro bean (securityFilterChain) que tambien requiere auth.
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http,
                                                       JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .cors(c -> c.configurationSource(corsSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(h -> h.frameOptions(f -> f.disable()));
        return http.build();
    }

    private CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
