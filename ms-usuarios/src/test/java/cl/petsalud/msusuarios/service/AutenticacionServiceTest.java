package cl.petsalud.msusuarios.service;

import cl.petsalud.msusuarios.dto.*;
import cl.petsalud.msusuarios.exception.ExcepcionNegocio;
import cl.petsalud.msusuarios.model.Rol;
import cl.petsalud.msusuarios.model.Usuario;
import cl.petsalud.msusuarios.repository.UsuarioRepository;
import cl.petsalud.msusuarios.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AutenticacionService autenticacionService;

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
    void registrarUsuarioCorrectamente() {

        RegistroRequest solicitud = new RegistroRequest(
                "12345678-5",
                "Juan",
                "Perez",
                "juan@gmail.com",
                "Juan123456"
        );

        when(usuarioRepository.existsByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(false);

        when(usuarioRepository.existsByRun(solicitud.run()))
                .thenReturn(false);

        when(passwordEncoder.encode(solicitud.password()))
                .thenReturn("passwordCodificada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario guardado = invocacion.getArgument(0);
                    guardado.setId(1L);
                    return guardado;
                });

        UsuarioResponse respuesta = autenticacionService.registrar(solicitud);

        assertEquals(1L, respuesta.id());
        assertEquals("juan@gmail.com", respuesta.correo());
        assertEquals(Rol.CLIENTE, respuesta.rol());

        verify(usuarioRepository).save(argThat(u ->
                "passwordCodificada".equals(u.getPassword())
                        && u.getRol() == Rol.CLIENTE
        ));
    }

    @Test
    void rechazarRegistroConCorreoDuplicado() {

        RegistroRequest solicitud = new RegistroRequest(
                "12345678-5",
                "Juan",
                "Perez",
                "juan@gmail.com",
                "Juan123456"
        );

        when(usuarioRepository.existsByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(true);

        ExcepcionNegocio excepcion = assertThrows(
                ExcepcionNegocio.class,
                () -> autenticacionService.registrar(solicitud)
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getEstado());
        assertEquals("CORREO_DUPLICADO", excepcion.getCodigo());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void iniciarSesionCorrectamente() {

        LoginRequest solicitud = new LoginRequest(
                "juan@gmail.com",
                "Juan123456"
        );

        when(usuarioRepository.findByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                solicitud.password(), usuario.getPassword()))
                .thenReturn(true);

        when(jwtService.generarAccessToken(usuario))
                .thenReturn("access-token-prueba");

        when(jwtService.generarRefreshToken(usuario))
                .thenReturn("refresh-token-prueba");

        AutenticacionResponse respuesta =
                autenticacionService.iniciarSesion(solicitud);

        assertEquals("access-token-prueba", respuesta.accessToken());
        assertEquals("refresh-token-prueba", respuesta.refreshToken());
        assertEquals(Rol.CLIENTE, respuesta.usuario().rol());
    }

    @Test
    void rechazarInicioSesionConPasswordIncorrecta() {

        LoginRequest solicitud = new LoginRequest(
                "juan@gmail.com",
                "incorrecta"
        );

        when(usuarioRepository.findByCorreoIgnoreCase(solicitud.correo()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                solicitud.password(), usuario.getPassword()))
                .thenReturn(false);

        ExcepcionNegocio excepcion = assertThrows(
                ExcepcionNegocio.class,
                () -> autenticacionService.iniciarSesion(solicitud)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, excepcion.getEstado());
        assertEquals("CREDENCIALES_INVALIDAS", excepcion.getCodigo());

        verifyNoInteractions(jwtService);
    }

    @Test
    void obtenerPerfilCorrectamente() {

        when(usuarioRepository.findByCorreoIgnoreCase("juan@gmail.com"))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse respuesta =
                autenticacionService.obtenerPerfil("juan@gmail.com");

        assertEquals(1L, respuesta.id());
        assertEquals("Juan", respuesta.nombre());
        assertEquals(Rol.CLIENTE, respuesta.rol());
    }

    @Test
    void rechazarTokenDeRenovacionInvalido() {

        RenovarTokenRequest solicitud =
                new RenovarTokenRequest("token-invalido");

        when(jwtService.validarToken("token-invalido", "REFRESH"))
                .thenReturn(false);

        ExcepcionNegocio excepcion = assertThrows(
                ExcepcionNegocio.class,
                () -> autenticacionService.renovarToken(solicitud)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, excepcion.getEstado());
        assertEquals("TOKEN_INVALIDO", excepcion.getCodigo());

        verifyNoInteractions(usuarioRepository);
    }
}
