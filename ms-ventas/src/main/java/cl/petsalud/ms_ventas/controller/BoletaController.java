package cl.petsalud.ms_ventas.controller;

import cl.petsalud.ms_ventas.dto.BoletaResponse;
import cl.petsalud.ms_ventas.dto.CompraRequest;
import cl.petsalud.ms_ventas.dto.EstadoRequest;
import cl.petsalud.ms_ventas.security.UsuarioAutenticado;
import cl.petsalud.ms_ventas.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@Tag(name = "Boletas", description = "Registro de compras y gestión de boletas")
public class BoletaController {

    private final BoletaService boletaService;

    @PostMapping
    @Operation(summary = "Registrar una compra (Público, asocia usuario si hay sesión)")
    public ResponseEntity<BoletaResponse> crear(
            @Valid @RequestBody CompraRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        Long usuarioId = usuario != null ? usuario.id() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.registrarCompra(request, usuarioId));
    }

    @GetMapping("/mis-boletas")
    @Operation(summary = "Obtener compras del usuario autenticado")
    public List<BoletaResponse> misBoletas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return boletaService.listarPorUsuario(usuario.id());
    }

    @GetMapping
    @Operation(summary = "Listar todas las boletas (ADMIN, VENDEDOR)")
    public List<BoletaResponse> listar() {
        return boletaService.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una boleta por ID (ADMIN, VENDEDOR)")
    public BoletaResponse obtener(@PathVariable Long id) {
        return boletaService.obtenerPorId(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Historial de compras de un usuario específico (ADMIN)")
    public List<BoletaResponse> porUsuario(@PathVariable Long usuarioId) {
        return boletaService.listarPorUsuario(usuarioId);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de boleta; si se anula, repone stock (ADMIN)")
    public BoletaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return boletaService.cambiarEstado(id, request.estado());
    }
}