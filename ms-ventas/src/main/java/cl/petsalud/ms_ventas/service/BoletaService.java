package cl.petsalud.ms_ventas.service;

import cl.petsalud.ms_ventas.dto.*;
import cl.petsalud.ms_ventas.exception.RecursoNoEncontradoException;
import cl.petsalud.ms_ventas.exception.ReglaNegocioException;
import cl.petsalud.ms_ventas.exception.ServicioNoDisponibleException;
import cl.petsalud.ms_ventas.model.Boleta;
import cl.petsalud.ms_ventas.model.DetalleBoleta;
import cl.petsalud.ms_ventas.model.EstadoBoleta;
import cl.petsalud.ms_ventas.repository.BoletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final RestClient productosRestClient;

    @Transactional
    public BoletaResponse registrarCompra(CompraRequest request, Long usuarioId) {
        List<DetalleBoleta> detalles = new ArrayList<>();
        List<StockItemDTO> itemsStock = new ArrayList<>();
        int totalBoleta = 0;

        for (ItemCompraRequest item : request.items()) {
            ProductoExternoDTO producto = obtenerProductoExterno(item.productoId());
            int precioReal = (Boolean.TRUE.equals(producto.enOferta()) && producto.precioOferta() != null)
                    ? producto.precioOferta()
                    : producto.precio();

            int subtotal = precioReal * item.cantidad();
            totalBoleta += subtotal;

            detalles.add(DetalleBoleta.builder()
                    .productoId(producto.id())
                    .nombreProducto(producto.nombre())
                    .cantidad(item.cantidad())
                    .precioUnitario(precioReal)
                    .subtotal(subtotal)
                    .build());

            itemsStock.add(new StockItemDTO(producto.id(), item.cantidad()));
        }

        descontarStockExterno(itemsStock);

        Boleta boleta = Boleta.builder()
                .fecha(LocalDateTime.now())
                .estado(EstadoBoleta.PAGADA)
                .total(totalBoleta)
                .usuarioId(usuarioId)
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .correo(request.correo())
                .calle(request.calle())
                .departamento(request.departamento())
                .region(request.region())
                .comuna(request.comuna())
                .indicaciones(request.indicaciones())
                .build();

        detalles.forEach(d -> d.setBoleta(boleta));
        boleta.setDetalles(detalles);

        try {
            Boleta guardada = boletaRepository.save(boleta);
            return BoletaResponse.from(guardada);
        } catch (Exception e) {
            reponerStockExterno(itemsStock);
            throw e;
        }
    }

    public List<BoletaResponse> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDesc().stream()
                .map(BoletaResponse::from)
                .toList();
    }

    public List<BoletaResponse> listarPorUsuario(Long usuarioId) {
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(BoletaResponse::from)
                .toList();
    }

    public BoletaResponse obtenerPorId(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada"));
        return BoletaResponse.from(boleta);
    }

    @Transactional
    public BoletaResponse cambiarEstado(Long id, EstadoBoleta nuevoEstado) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada"));

        if (boleta.getEstado() == EstadoBoleta.ANULADA) {
            throw new ReglaNegocioException("La boleta ya se encuentra anulada");
        }

        if (nuevoEstado == EstadoBoleta.ANULADA) {
            List<StockItemDTO> items = boleta.getDetalles().stream()
                    .map(d -> new StockItemDTO(d.getProductoId(), d.getCantidad()))
                    .toList();
            reponerStockExterno(items);
        }

        boleta.setEstado(nuevoEstado);
        return BoletaResponse.from(boletaRepository.save(boleta));
    }

    private ProductoExternoDTO obtenerProductoExterno(Long productoId) {
        try {
            return productosRestClient.get()
                    .uri("/api/v1/productos/{id}", productoId)
                    .retrieve()
                    .body(ProductoExternoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Producto con id " + productoId + " no existe");
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("Servicio de productos no disponible");
        }
    }

    private void descontarStockExterno(List<StockItemDTO> items) {
        try {
            productosRestClient.post()
                    .uri("/api/v1/productos/stock/descontar")
                    .header("X-Internal-Service", "ms-ventas")
                    .body(items)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new ReglaNegocioException("Stock insuficiente para uno o más productos");
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("Servicio de productos no disponible");
        }
    }

    private void reponerStockExterno(List<StockItemDTO> items) {
        try {
            productosRestClient.post()
                    .uri("/api/v1/productos/stock/reponer")
                    .header("X-Internal-Service", "ms-ventas")
                    .body(items)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("Servicio de productos no disponible");
        }
    }
}