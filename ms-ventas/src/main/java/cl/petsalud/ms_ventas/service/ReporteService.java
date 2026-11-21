package cl.petsalud.ms_ventas.service;

import cl.petsalud.ms_ventas.dto.ResumenVentasResponse;
import cl.petsalud.ms_ventas.model.Boleta;
import cl.petsalud.ms_ventas.model.DetalleBoleta;
import cl.petsalud.ms_ventas.model.EstadoBoleta;
import cl.petsalud.ms_ventas.repository.BoletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final BoletaRepository boletaRepository;

    public ResumenVentasResponse obtenerResumen() {
        List<Boleta> validas = boletaRepository.findAll().stream()
                .filter(b -> b.getEstado() != EstadoBoleta.ANULADA)
                .toList();

        long cantidad = validas.size();
        long totalVendido = validas.stream().mapToLong(Boleta::getTotal).sum();
        double ticketPromedio = cantidad > 0 ? (double) totalVendido / cantidad : 0.0;

        Map<Long, String> nombres = new HashMap<>();
        Map<Long, Long> cantidades = new HashMap<>();

        for (Boleta boleta : validas) {
            for (DetalleBoleta det : boleta.getDetalles()) {
                nombres.put(det.getProductoId(), det.getNombreProducto());
                cantidades.merge(det.getProductoId(), det.getCantidad().longValue(), Long::sum);
            }
        }

        List<ResumenVentasResponse.ProductoVendidoDTO> topProductos = cantidades.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> new ResumenVentasResponse.ProductoVendidoDTO(e.getKey(), nombres.get(e.getKey()), e.getValue()))
                .toList();

        return new ResumenVentasResponse(totalVendido, cantidad, ticketPromedio, topProductos);
    }
}