package cl.petsalud.msusuarios.config;

import cl.petsalud.msusuarios.model.Rol;
import cl.petsalud.msusuarios.model.Usuario;
import cl.petsalud.msusuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario administrador = crearUsuario(
                "11111111-1",
                "Administrador",
                "PetSalud",
                "admin@petsalud.cl",
                "Admin1234",
                Rol.ADMIN
        );

        Usuario cliente = crearUsuario(
                "33333333-3",
                "Cliente",
                "PetSalud",
                "cliente@petsalud.cl",
                "Cliente1234",
                Rol.CLIENTE
        );

        usuarioRepository.saveAll(
                List.of(administrador, cliente)
        );
    }

    private Usuario crearUsuario(
            String run,
            String nombre,
            String apellidos,
            String correo,
            String password,
            Rol rol) {

        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);

        return usuario;
    }
}
