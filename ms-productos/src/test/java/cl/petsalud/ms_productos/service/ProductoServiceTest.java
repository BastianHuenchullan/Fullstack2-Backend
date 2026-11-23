package cl.petsalud.ms_productos.service;

import cl.petsalud.ms_productos.dto.ProductoRequest;
import cl.petsalud.ms_productos.dto.ProductoResponse;
import cl.petsalud.ms_productos.dto.StockMovimientoRequest;
import cl.petsalud.ms_productos.exception.ReglaNegocioException;
import cl.petsalud.ms_productos.exception.StockInsuficienteException;
import cl.petsalud.ms_productos.model.Categoria;
import cl.petsalud.ms_productos.model.Producto;
import cl.petsalud.ms_productos.repository.CategoriaRepository;
import cl.petsalud.ms_productos.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void prepararDatos() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Perros")
                .build();

        producto = Producto.builder()
                .id(1L)
                .codigo("PROD-001")
                .nombre("ALFA DOG")
                .descripcion("Alimento completo")
                .precio(22990)
                .precioOferta(19990)
                .enOferta(true)
                .stock(10)
                .stockCritico(5)
                .imagenUrl("/assets/producto_1.png")
                .categoria(categoria)
                .build();

    }
    @Test
    void crearProductoEnOfertaCorrectamente() {
        ProductoRequest request = new ProductoRequest(
                "prod-010",
                "Alimento Natural",
                "Saco 10kg",
                25000,
                21000,
                true,
                15,
                3,
                "/assets/producto_1.png",
                1L
        );

        when(productoRepository.existsByCodigoIgnoreCase("PROD-010")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto guardado = inv.getArgument(0);
            guardado.setId(10L);
            return guardado;
        });

        ProductoResponse response = productoService.crear(request);

        assertEquals(10L, response.id());
        assertEquals("PROD-010", response.codigo());
        assertTrue(response.enOferta());
        assertEquals("Perros", response.categoriaNombre());
    }

    @Test
    void rechazarProductoConPrecioOfertaMayorAlNormal() {
        ProductoRequest request = new ProductoRequest(
                "PROD-011",
                "Alimento Premium",
                "Saco 5kg",
                20000,
                22000,
                true,
                10,
                2,
                null,
                1L
        );

        when(productoRepository.existsByCodigoIgnoreCase("PROD-011")).thenReturn(false);

        assertThrows(ReglaNegocioException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void descontarStockCorrectamente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.descontarStock(List.of(new StockMovimientoRequest(1L, 3)));

        assertEquals(7, producto.getStock());
        verify(productoRepository).save(producto);
    }

    @Test
    void rechazarDescuentoCuandoStockEsInsuficiente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        List<StockMovimientoRequest> items = List.of(new StockMovimientoRequest(1L, 15));

        assertThrows(StockInsuficienteException.class, () -> productoService.descontarStock(items));
        assertEquals(10, producto.getStock());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void reponerStockCorrectamente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.reponerStock(List.of(new StockMovimientoRequest(1L, 4)));

        assertEquals(14, producto.getStock());
        verify(productoRepository).save(producto);
    }
}