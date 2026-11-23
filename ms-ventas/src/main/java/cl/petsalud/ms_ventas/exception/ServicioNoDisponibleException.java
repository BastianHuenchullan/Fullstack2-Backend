package cl.petsalud.ms_ventas.exception;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String message) {
        super(message);
    }
}