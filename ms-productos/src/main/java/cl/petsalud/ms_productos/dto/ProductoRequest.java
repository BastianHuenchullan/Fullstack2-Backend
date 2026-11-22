package cl.petsalud.ms_productos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 30, message = "El codigo no puede superar los 30 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        Integer precio,

        @Positive(message = "El precio de oferta debe ser mayor a 0")
        Integer precioOferta,

        boolean enOferta,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El stock crítico es obligatorio")
        @PositiveOrZero(message = "El stock crítico no puede ser negativo")
        Integer stockCritico,

        @Size(max = 255, message = "La URL de la imagen no puede superar los 255 caracteres")
        String imagenUrl,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId
) {
}