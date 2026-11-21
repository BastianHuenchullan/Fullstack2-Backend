package cl.petsalud.msusuarios.dto;

public record TokensResponse(
        String accessToken,
        String refreshToken
) {
}
