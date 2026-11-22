package cl.petsalud.ms_productos.service;

import cl.petsalud.ms_productos.dto.CategoriaRequest;
import cl.petsalud.ms_productos.dto.CategoriaResponse;
import cl.petsalud.ms_productos.exception.ReglaNegocioException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void prepararDatos() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Perros")
                .descripcion("Alimentos para perros")
                .build();
    }

    @Test
    void listarCategoriasCorrectamente() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<CategoriaResponse> resultado = categoriaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Perros", resultado.get(0).nombre());
    }

    @Test
    void crearCategoriaCorrectamente() {
        CategoriaRequest request = new CategoriaRequest("Accesorios", "Juguetes y correas");

        when(categoriaRepository.existsByNombreIgnoreCase("Accesorios")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> {
            Categoria guardada = inv.getArgument(0);
            guardada.setId(2L);
            return guardada;
        });

        CategoriaResponse response = categoriaService.crear(request);

        assertEquals(2L, response.id());
        assertEquals("Accesorios", response.nombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void rechazarCreacionConNombreDuplicado() {
        CategoriaRequest request = new CategoriaRequest("Perros", "Duplicada");

        when(categoriaRepository.existsByNombreIgnoreCase("Perros")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.crear(request));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void impedirEliminarCategoriaConProductosAsociados() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(new Producto()));

        assertThrows(ReglaNegocioException.class, () -> categoriaService.eliminar(1L));
        verify(categoriaRepository, never()).delete(any());
    }
}