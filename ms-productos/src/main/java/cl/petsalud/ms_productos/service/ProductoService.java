package cl.petsalud.ms_productos.service;

import cl.petsalud.ms_productos.dto.ProductoRequest;
import cl.petsalud.ms_productos.dto.ProductoResponse;
import cl.petsalud.ms_productos.dto.StockMovimientoRequest;
import cl.petsalud.ms_productos.exception.RecursoNoEncontradoException;
import cl.petsalud.ms_productos.exception.ReglaNegocioException;
import cl.petsalud.ms_productos.exception.StockInsuficienteException;
import cl.petsalud.ms_productos.model.Categoria;
import cl.petsalud.ms_productos.model.Producto;
import cl.petsalud.ms_productos.repository.CategoriaRepository;
import cl.petsalud.ms_productos.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, String categoria, Boolean ofertas, String buscar) {
        List<Producto> productos;

        if (categoriaId != null) {
            productos = productoRepository.findByCategoriaId(categoriaId);
        } else if (categoria != null && !categoria.isBlank()) {
            productos = productoRepository.findByCategoriaNombreIgnoreCase(categoria.trim());
        } else if (Boolean.TRUE.equals(ofertas)) {
            productos = productoRepository.findByEnOfertaTrue();
        } else if (buscar != null && !buscar.isBlank()) {
            productos = productoRepository.findByNombreContainingIgnoreCase(buscar.trim());
        } else {
            productos = productoRepository.findAll();
        }

        return productos.stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return convertirResponse(buscarProducto(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarStockCritico() {
        return productoRepository.findProductosConStockCritico()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String codigo = request.codigo().trim().toUpperCase();

        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + codigo);
        }

        validarOferta(request);
        Categoria categoria = buscarCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .codigo(codigo)
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion())
                .precio(request.precio())
                .precioOferta(request.enOferta() ? request.precioOferta() : null)
                .enOferta(request.enOferta())
                .stock(request.stock())
                .stockCritico(request.stockCritico())
                .imagenUrl(request.imagenUrl())
                .categoria(categoria)
                .build();

        return convertirResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarProducto(id);
        String codigo = request.codigo().trim().toUpperCase();

        if (productoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("Ya existe otro producto con el código " + codigo);
        }

        validarOferta(request);
        Categoria categoria = buscarCategoria(request.categoriaId());

        producto.setCodigo(codigo);
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setPrecioOferta(request.enOferta() ? request.precioOferta() : null);
        producto.setEnOferta(request.enOferta());
        producto.setStock(request.stock());
        producto.setStockCritico(request.stockCritico());
        producto.setImagenUrl(request.imagenUrl());
        producto.setCategoria(categoria);

        return convertirResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarProducto(id);
        productoRepository.delete(producto);
    }

    @Transactional
    public void descontarStock(List<StockMovimientoRequest> items) {
        for (StockMovimientoRequest item : items) {
            Producto producto = buscarProducto(item.productoId());
            if (producto.getStock() < item.cantidad()) {
                throw new StockInsuficienteException("Stock insuficiente para el producto: " + producto.getNombre());
            }
        }

        for (StockMovimientoRequest item : items) {
            Producto producto = buscarProducto(item.productoId());
            producto.setStock(producto.getStock() - item.cantidad());
            productoRepository.save(producto);
        }
    }

    @Transactional
    public void reponerStock(List<StockMovimientoRequest> items) {
        for (StockMovimientoRequest item : items) {
            Producto producto = buscarProducto(item.productoId());
            producto.setStock(producto.getStock() + item.cantidad());
            productoRepository.save(producto);
        }
    }

    private void validarOferta(ProductoRequest request) {
        if (request.enOferta()) {
            if (request.precioOferta() == null) {
                throw new ReglaNegocioException("Debes indicar el precio de oferta");
            }
            if (request.precioOferta() >= request.precio()) {
                throw new ReglaNegocioException("El precio de oferta debe ser menor al precio normal");
            }
        }
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    private ProductoResponse convertirResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getPrecioOferta(),
                producto.isEnOferta(),
                producto.getStock(),
                producto.getStockCritico(),
                producto.getImagenUrl(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }
}