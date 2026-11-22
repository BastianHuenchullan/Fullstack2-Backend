package cl.petsalud.ms_productos.service;

import cl.petsalud.ms_productos.dto.CategoriaRequest;
import cl.petsalud.ms_productos.dto.CategoriaResponse;
import cl.petsalud.ms_productos.exception.RecursoNoEncontradoException;
import cl.petsalud.ms_productos.exception.ReglaNegocioException;
import cl.petsalud.ms_productos.model.Categoria;
import cl.petsalud.ms_productos.repository.CategoriaRepository;
import cl.petsalud.ms_productos.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        return convertirResponse(buscarCategoria(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .descripcion(request.descripcion())
                .build();

        return convertirResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarCategoria(id);
        String nombre = request.nombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ReglaNegocioException("Ya existe otra categoría con ese nombre");
                });

        categoria.setNombre(nombre);
        categoria.setDescripcion(request.descripcion());

        return convertirResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarCategoria(id);

        if (!productoRepository.findByCategoriaId(id).isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar una categoría que tiene productos asociados");
        }

        categoriaRepository.delete(categoria);
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    private CategoriaResponse convertirResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}