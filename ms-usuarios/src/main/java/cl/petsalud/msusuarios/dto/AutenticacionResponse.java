package cl.petsalud.msusuarios.dto;

public record AutenticacionResponse(
        String accessToken,
        String refreshToken,
        UsuarioResponse usuario
) {
}
