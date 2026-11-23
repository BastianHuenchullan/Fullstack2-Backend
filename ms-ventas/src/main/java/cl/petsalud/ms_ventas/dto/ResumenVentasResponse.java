package cl.petsalud.ms_ventas.dto;

import java.util.List;

public record ResumenVentasResponse(
        Long totalVendido,
        Long cantidadBoletas,
        Double ticketPromedio,
        List<ProductoVendidoDTO> productosMasVendidos
) {
    public record ProductoVendidoDTO(Long productoId, String nombreProducto, Long unidadesVendidas) {}
}