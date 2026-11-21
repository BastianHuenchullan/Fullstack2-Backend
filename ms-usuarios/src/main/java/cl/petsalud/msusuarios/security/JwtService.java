package cl.petsalud.msusuarios.security;

import cl.petsalud.msusuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;

    public JwtService(@Value("${jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(
                secreto.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generarAccessToken(Usuario usuario) {
        return generarToken(usuario, "ACCESS", Duration.ofMinutes(15));
    }

    public String generarRefreshToken(Usuario usuario) {
        return generarToken(usuario, "REFRESH", Duration.ofDays(7));
    }

    private String generarToken(Usuario usuario, String tipo, Duration duracion) {
        Instant ahora = Instant.now();

        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("userId", usuario.getId())
                .claim("tipo", tipo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    public Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validarToken(String token, String tipo) {
        try {
            Claims claims = obtenerClaims(token);

            return tipo.equals(claims.get("tipo", String.class))
                    && claims.getSubject() != null
                    && !claims.getSubject().isBlank()
                    && claims.get("rol") instanceof String
                    && claims.get("userId") instanceof Number;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
