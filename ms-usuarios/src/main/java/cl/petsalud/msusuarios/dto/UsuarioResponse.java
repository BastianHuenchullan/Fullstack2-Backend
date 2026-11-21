package cl.petsalud.msusuarios.dto;

import cl.petsalud.msusuarios.model.Rol;

public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        Rol rol
) {
}
