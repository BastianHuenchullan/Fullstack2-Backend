package cl.petsalud.msusuarios.security;

import cl.petsalud.msusuarios.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/registro",
                                "/api/v1/auth/refresh"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/usuarios",
                                "/api/v1/usuarios/**"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, error) ->
                                escribirError(
                                        response,
                                        401,
                                        "NO_AUTORIZADO",
                                        "Debes iniciar sesión"
                                ))
                        .accessDeniedHandler((request, response, error) ->
                                escribirError(
                                        response,
                                        403,
                                        "ACCESO_DENEGADO",
                                        "No tienes permisos para esta operación"
                                ))
                )
                .addFilterBefore(
                        new JwtAuthFilter(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOrigins(
                List.of("http://localhost:3000")
        );

        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuracion.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        UrlBasedCorsConfigurationSource origen =
                new UrlBasedCorsConfigurationSource();

        origen.registerCorsConfiguration("/**", configuracion);

        return origen;
    }

    private void escribirError(
            HttpServletResponse response,
            int estado,
            String codigo,
            String mensaje
    ) throws IOException {

        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"codigo\":\"" + codigo +
                        "\",\"mensaje\":\"" + mensaje +
                        "\",\"errores\":[]}"
        );
    }
}
