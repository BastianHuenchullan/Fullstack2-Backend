package cl.petsalud.ms_ventas.security;

public record UsuarioAutenticado(Long id, String correo, String rol) {}