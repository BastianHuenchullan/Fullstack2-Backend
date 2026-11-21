package cl.petsalud.msusuarios.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<Map<String, Object>> manejarNegocio(
            ExcepcionNegocio excepcion) {

        return crearRespuesta(
                excepcion.getEstado(),
                excepcion.getCodigo(),
                excepcion.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException excepcion) {

        List<String> errores = excepcion.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " +
                        error.getDefaultMessage())
                .toList();

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "DATOS_INVALIDOS",
                "Revisa los datos ingresados",
                errores
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarGeneral(
            Exception excepcion) {

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrió un error inesperado",
                List.of()
        );
    }

    private ResponseEntity<Map<String, Object>> crearRespuesta(
            HttpStatus estado,
            String codigo,
            String mensaje,
            List<String> errores) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("codigo", codigo);
        respuesta.put("mensaje", mensaje);
        respuesta.put("errores", errores);

        return ResponseEntity.status(estado).body(respuesta);
    }
}
