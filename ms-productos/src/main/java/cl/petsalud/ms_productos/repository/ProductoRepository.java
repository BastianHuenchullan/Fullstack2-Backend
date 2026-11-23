package cl.petsalud.ms_productos.repository;

import cl.petsalud.ms_productos.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    List<Producto> findByCategoriaId(Long categoriaId);

    List<Producto> findByCategoriaNombreIgnoreCase(String categoriaNombre);

    List<Producto> findByEnOfertaTrue();

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockCritico")
    List<Producto> findProductosConStockCritico();
}