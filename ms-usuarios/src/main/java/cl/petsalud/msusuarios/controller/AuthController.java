package cl.petsalud.msusuarios.controller;

import cl.petsalud.msusuarios.dto.AutenticacionResponse;
import cl.petsalud.msusuarios.dto.LoginRequest;
import cl.petsalud.msusuarios.dto.RegistroRequest;
import cl.petsalud.msusuarios.dto.RenovarTokenRequest;
import cl.petsalud.msusuarios.dto.TokensResponse;
import cl.petsalud.msusuarios.dto.UsuarioResponse;
import cl.petsalud.msusuarios.service.AutenticacionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticacionService autenticacionService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest solicitud) {

        UsuarioResponse usuario = autenticacionService.registrar(solicitud);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PostMapping("/login")
    public AutenticacionResponse iniciarSesion(
            @Valid @RequestBody LoginRequest solicitud) {

        return autenticacionService.iniciarSesion(solicitud);
    }

    @PostMapping("/refresh")
    public TokensResponse renovarToken(
            @Valid @RequestBody RenovarTokenRequest solicitud) {

        return autenticacionService.renovarToken(solicitud);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public UsuarioResponse obtenerPerfil(Authentication autenticacion) {

        return autenticacionService.obtenerPerfil(autenticacion.getName());
    }
}
