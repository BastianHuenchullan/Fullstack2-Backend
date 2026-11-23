package cl.petsalud.ms_ventas.dto;

import cl.petsalud.ms_ventas.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoBoleta estado
) {}