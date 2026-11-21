package cl.petsalud.msusuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record RenovarTokenRequest(

        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken

) {
}
