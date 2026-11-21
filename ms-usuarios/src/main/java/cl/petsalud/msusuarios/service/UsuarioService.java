package cl.petsalud.msusuarios.service;

import cl.petsalud.msusuarios.dto.UsuarioRequest;
import cl.petsalud.msusuarios.dto.UsuarioResponse;
import cl.petsalud.msusuarios.exception.ExcepcionNegocio;
import cl.petsalud.msusuarios.model.Usuario;
import cl.petsalud.msusuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {

        return usuarioRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {

        return convertirResponse(buscarUsuario(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest solicitud) {

        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
        String run = solicitud.run().trim();

        validarDuplicados(run, correo, null);
        validarPassword(solicitud.password());

        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellidos(solicitud.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(solicitud.password()));
        usuario.setRol(solicitud.rol());

        return convertirResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest solicitud) {

        Usuario usuario = buscarUsuario(id);

        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);
        String run = solicitud.run().trim();

        validarDuplicados(run, correo, id);

        usuario.setRun(run);
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellidos(solicitud.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setRol(solicitud.rol());

        if (solicitud.password() != null
                && !solicitud.password().isBlank()) {

            validarPassword(solicitud.password());

            usuario.setPassword(
                    passwordEncoder.encode(solicitud.password())
            );
        }

        return convertirResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String correoAutenticado) {

        Usuario usuario = buscarUsuario(id);

        if (usuario.getCorreo().equalsIgnoreCase(correoAutenticado)) {
            throw new ExcepcionNegocio(
                    HttpStatus.CONFLICT,
                    "ELIMINACION_NO_PERMITIDA",
                    "No puedes eliminar tu propia cuenta"
            );
        }

        usuarioRepository.delete(usuario);
    }

    private Usuario buscarUsuario(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionNegocio(
                        HttpStatus.NOT_FOUND,
                        "USUARIO_NO_ENCONTRADO",
                        "El usuario no existe"
                ));
    }

    private void validarDuplicados(String run, String correo, Long id) {

        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(usuario -> !usuario.getId().equals(id))
                .ifPresent(usuario -> {
                    throw new ExcepcionNegocio(
                            HttpStatus.CONFLICT,
                            "CORREO_DUPLICADO",
                            "El correo ya está registrado"
                    );
                });

        usuarioRepository.findByRun(run)
                .filter(usuario -> !usuario.getId().equals(id))
                .ifPresent(usuario -> {
                    throw new ExcepcionNegocio(
                            HttpStatus.CONFLICT,
                            "RUN_DUPLICADO",
                            "El RUN ya está registrado"
                    );
                });
    }

    private void validarPassword(String password) {

        if (password == null || password.isBlank()
                || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ExcepcionNegocio(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_INVALIDA",
                    "La contraseña debe tener al menos 8 caracteres y un máximo de 72 bytes"
            );
        }
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
