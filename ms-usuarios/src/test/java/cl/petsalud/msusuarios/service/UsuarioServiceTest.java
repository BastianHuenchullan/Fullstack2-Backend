package cl.petsalud.msusuarios.service;

import cl.petsalud.msusuarios.dto.UsuarioRequest;
import cl.petsalud.msusuarios.dto.UsuarioResponse;
import cl.petsalud.msusuarios.exception.ExcepcionNegocio;
import cl.petsalud.msusuarios.model.Rol;
import cl.petsalud.msusuarios.model.Usuario;
import cl.petsalud.msusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void prepararDatos() {
        usuario = new Usuario(
                1L,
                "12345678-5",
                "Juan",
                "Perez",
                "juan@gmail.com",
                "passwordCodificada",
                Rol.CLIENTE
        );
    }

    @Test
    void listarUsuariosCorrectamente() {

        when(usuarioRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(usuario));

        List<UsuarioResponse> respuesta = usuarioService.listar();

        assertEquals(1, respuesta.size());
        assertEquals("Juan", respuesta.get(0).nombre());
        assertEquals(Rol.CLIENTE, respuesta.get(0).rol());
    }

    @Test
    void obtenerUsuarioCorrectamente() {

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse respuesta = usuarioService.obtener(1L);

        assertEquals(1L, respuesta.id());
        assertEquals("juan@gmail.com", respuesta.correo());
    }

    @Test
    void rechazarBusquedaDeUsuarioInexistente() {

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ExcepcionNegocio excepcion = assertThrows(
                ExcepcionNegocio.class,
                () -> usuarioService.obtener(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getEstado());
        assertEquals("USUARIO_NO_ENCONTRADO", excepcion.getCodigo());
    }

    @Test
    void crearUsuarioCorrectamente() {

        UsuarioRequest solicitud = new UsuarioRequest(
                "9876543-3",
                "Carlos",
                "Gonzalez",
                "carlos@petsalud.cl",
                "Carlos1234",
                Rol.CLIENTE
        );

        when(usuarioRepository.findByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(Optional.empty());

        when(usuarioRepository.findByRun(solicitud.run()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(solicitud.password()))
                .thenReturn("passwordCodificada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario guardado = invocacion.getArgument(0);
                    guardado.setId(2L);
                    return guardado;
                });

        UsuarioResponse respuesta = usuarioService.crear(solicitud);

        assertEquals(2L, respuesta.id());
        assertEquals("Carlos", respuesta.nombre());
        assertEquals(Rol.CLIENTE, respuesta.rol());

        verify(usuarioRepository).save(argThat(u ->
                "passwordCodificada".equals(u.getPassword())
                        && u.getRol() == Rol.CLIENTE
        ));
    }

    @Test
    void rechazarCreacionConCorreoDuplicado() {

        UsuarioRequest solicitud = new UsuarioRequest(
                "9876543-3",
                "Carlos",
                "Gonzalez",
                "juan@gmail.com",
                "Carlos1234",
                Rol.CLIENTE
        );

        when(usuarioRepository.findByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(Optional.of(usuario));

        ExcepcionNegocio excepcion = assertThrows(
                ExcepcionNegocio.class,
                () -> usuarioService.crear(solicitud)
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getEstado());
        assertEquals("CORREO_DUPLICADO", excepcion.getCodigo());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuarioConservandoPassword() {

        UsuarioRequest solicitud = new UsuarioRequest(
                "12345678-5",
                "Juan",
                "Perez",
                "juan@gmail.com",
                "",
                Rol.ADMIN
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.findByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.findByRun(solicitud.run()))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        UsuarioResponse respuesta =
                usuarioService.actualizar(1L, solicitud);

        assertEquals(Rol.ADMIN, respuesta.rol());
        assertEquals("passwordCodificada", usuario.getPassword());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void eliminarUsuarioCorrectamente() {

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L, "admin@petsalud.cl");

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void impedirEliminarLaPropiaCuenta() {

        usuario.setRol(Rol.ADMIN);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        ExcepcionNegocio excepcion = assertThrows(
                ExcepcionNegocio.class,
                () -> usuarioService.eliminar(1L, "juan@gmail.com")
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getEstado());
        assertEquals("ELIMINACION_NO_PERMITIDA", excepcion.getCodigo());

        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }
}
