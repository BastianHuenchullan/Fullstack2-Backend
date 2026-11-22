package cl.petsalud.ms_productos.dto;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer precio,
        Integer precioOferta,
        boolean enOferta,
        Integer stock,
        Integer stockCritico,
        String imagenUrl,
        Long categoriaId,
        String categoriaNombre
) {
}