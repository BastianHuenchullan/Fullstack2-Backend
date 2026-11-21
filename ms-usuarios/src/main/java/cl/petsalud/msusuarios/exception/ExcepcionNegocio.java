package cl.petsalud.msusuarios.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExcepcionNegocio extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;

    public ExcepcionNegocio(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }
}
