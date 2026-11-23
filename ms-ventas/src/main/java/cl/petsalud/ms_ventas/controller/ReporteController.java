package cl.petsalud.ms_ventas.controller;

import cl.petsalud.ms_ventas.dto.ResumenVentasResponse;
import cl.petsalud.ms_ventas.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Métricas y resumen de ventas")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de ventas excluyendo boletas anuladas (ADMIN)")
    public ResumenVentasResponse resumen() {
        return reporteService.obtenerResumen();
    }
}