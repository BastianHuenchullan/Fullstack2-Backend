package cl.petsalud.ms_ventas.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        int codigo,
        String mensaje,
        Map<String, String> errores,
        LocalDateTime timestamp
) {
    public ApiError(int codigo, String mensaje) {
        this(codigo, mensaje, null, LocalDateTime.now());
    }
}