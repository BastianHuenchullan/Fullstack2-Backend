package cl.petsalud.ms_ventas.dto;

public record ProductoExternoDTO(
        Long id,
        String nombre,
        Integer precio,
        Integer precioOferta,
        Boolean enOferta,
        Integer stock
) {}