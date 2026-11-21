package cl.petsalud.msusuarios.security;

import cl.petsalud.msusuarios.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")) {

            String token = encabezado.substring(7);

            try {
                Claims claims = jwtService.obtenerClaims(token);

                if ("ACCESS".equals(claims.get("tipo", String.class))
                        && claims.get("userId") instanceof Number userId
                        && claims.getSubject() != null) {

                    usuarioRepository.findByCorreoIgnoreCase(claims.getSubject())
                            .filter(usuario -> usuario.getId().equals(userId.longValue()))
                            .ifPresent(usuario -> {

                                var permisos = List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + usuario.getRol().name()
                                        )
                                );

                                var autenticacion =
                                        new UsernamePasswordAuthenticationToken(
                                                usuario.getCorreo(),
                                                null,
                                                permisos
                                        );

                                SecurityContext contexto =
                                        SecurityContextHolder.createEmptyContext();

                                contexto.setAuthentication(autenticacion);
                                SecurityContextHolder.setContext(contexto);
                            });
                }

            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
