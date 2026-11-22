package cl.petsalud.ms_productos.controller;

import cl.petsalud.ms_productos.dto.ProductoRequest;
import cl.petsalud.ms_productos.dto.ProductoResponse;
import cl.petsalud.ms_productos.dto.StockMovimientoRequest;
import cl.petsalud.ms_productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de productos y control de inventario")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos con filtros opcionales")
    public List<ProductoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean ofertas,
            @RequestParam(required = false) String buscar) {
        return productoService.listar(categoriaId, categoria, ofertas, buscar);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Listar productos con stock crítico (ADMIN)")
    public List<ProductoResponse> stockCritico() {
        return productoService.listarStockCritico();
    }

    @PostMapping
    @Operation(summary = "Crear producto (ADMIN)")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto (ADMIN)")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto (ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stock/descontar")
    @Operation(summary = "Descontar stock tras una venta")
    public ResponseEntity<Void> descontarStock(@Valid @RequestBody List<StockMovimientoRequest> items) {
        productoService.descontarStock(items);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock/reponer")
    @Operation(summary = "Reponer stock por anulación de venta")
    public ResponseEntity<Void> reponerStock(@Valid @RequestBody List<StockMovimientoRequest> items) {
        productoService.reponerStock(items);
        return ResponseEntity.ok().build();
    }
}