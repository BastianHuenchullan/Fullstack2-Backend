package cl.petsalud.msusuarios.dto;

import cl.petsalud.msusuarios.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "El RUN es obligatorio")
        @Size(max = 12, message = "El RUN no puede superar los 12 caracteres")
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 255)
        String correo,

        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol

) {
}
