package cl.petsalud.msusuarios.service;

import cl.petsalud.msusuarios.dto.*;
import cl.petsalud.msusuarios.exception.ExcepcionNegocio;
import cl.petsalud.msusuarios.model.Rol;
import cl.petsalud.msusuarios.model.Usuario;
import cl.petsalud.msusuarios.repository.UsuarioRepository;
import cl.petsalud.msusuarios.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioResponse registrar(RegistroRequest solicitud) {

        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
        String run = solicitud.run().trim();

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ExcepcionNegocio(
                    HttpStatus.CONFLICT,
                    "CORREO_DUPLICADO",
                    "El correo ya está registrado"
            );
        }

        if (usuarioRepository.existsByRun(run)) {
            throw new ExcepcionNegocio(
                    HttpStatus.CONFLICT,
                    "RUN_DUPLICADO",
                    "El RUN ya está registrado"
            );
        }

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellidos(solicitud.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(solicitud.password()));
        usuario.setRol(Rol.CLIENTE);

        Usuario guardado = usuarioRepository.save(usuario);

        return convertirResponse(guardado);
    }

    public AutenticacionResponse iniciarSesion(LoginRequest solicitud) {

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(solicitud.correo().trim())
                .orElseThrow(() -> new ExcepcionNegocio(
                        HttpStatus.UNAUTHORIZED,
                        "CREDENCIALES_INVALIDAS",
                        "Correo o contraseña incorrectos"
                ));

        if (!passwordEncoder.matches(
                solicitud.password(), usuario.getPassword())) {
            throw new ExcepcionNegocio(
                    HttpStatus.UNAUTHORIZED,
                    "CREDENCIALES_INVALIDAS",
                    "Correo o contraseña incorrectos"
            );
        }

        return new AutenticacionResponse(
                jwtService.generarAccessToken(usuario),
                jwtService.generarRefreshToken(usuario),
                convertirResponse(usuario)
        );
    }

    public TokensResponse renovarToken(RenovarTokenRequest solicitud) {

        String token = solicitud.refreshToken();

        if (!jwtService.validarToken(token, "REFRESH")) {
            throw new ExcepcionNegocio(
                    HttpStatus.UNAUTHORIZED,
                    "TOKEN_INVALIDO",
                    "El token de renovación no es válido"
            );
        }

        Claims claims = jwtService.obtenerClaims(token);
        Long userId = ((Number) claims.get("userId")).longValue();

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(claims.getSubject())
                .filter(u -> u.getId().equals(userId))
                .orElseThrow(() -> new ExcepcionNegocio(
                        HttpStatus.UNAUTHORIZED,
                        "TOKEN_INVALIDO",
                        "El usuario del token no es válido"
                ));

        return new TokensResponse(
                jwtService.generarAccessToken(usuario),
                jwtService.generarRefreshToken(usuario)
        );
    }

    public UsuarioResponse obtenerPerfil(String correo) {

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ExcepcionNegocio(
                        HttpStatus.NOT_FOUND,
                        "USUARIO_NO_ENCONTRADO",
                        "El usuario no existe"
                ));

        return convertirResponse(usuario);
    }

    private UsuarioResponse convertirResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}
