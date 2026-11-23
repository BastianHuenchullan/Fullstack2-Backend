package cl.petsalud.ms_productos.dto;

import java.util.List;

public record ErrorResponse(
        int codigo,
        String mensaje,
        List<String> errores
) {
}