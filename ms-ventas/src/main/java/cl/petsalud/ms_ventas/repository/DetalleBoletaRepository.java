package cl.petsalud.ms_ventas.repository;

import cl.petsalud.ms_ventas.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {
}