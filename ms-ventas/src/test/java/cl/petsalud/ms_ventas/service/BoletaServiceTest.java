package cl.petsalud.ms_ventas.service;

import cl.petsalud.ms_ventas.dto.*;
import cl.petsalud.ms_ventas.exception.ReglaNegocioException;
import cl.petsalud.ms_ventas.model.Boleta;
import cl.petsalud.ms_ventas.repository.BoletaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private RestClient productosRestClient;

    @Mock
    private RestClient.RequestHeadersUriSpec getUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec getHeadersSpec;

    @Mock
    private RestClient.RequestBodyUriSpec postUriSpec;

    @Mock
    private RestClient.RequestBodySpec postBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private BoletaService boletaService;

    @Test
    void compraCalculaTotalUsandoPrecioOferta() {
        ProductoExternoDTO producto = new ProductoExternoDTO(1L, "Alimento Perro", 20000, 15000, true, 10);

        when(productosRestClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri("/api/v1/productos/{id}", 1L)).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProductoExternoDTO.class)).thenReturn(producto);

        when(productosRestClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri("/api/v1/productos/stock/descontar")).thenReturn(postBodySpec);
        when(postBodySpec.header(any(), any())).thenReturn(postBodySpec);
        when(postBodySpec.body(any(List.class))).thenReturn(postBodySpec);
        when(postBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        CompraRequest request = new CompraRequest(
                "Juan", "Perez", "juan@mail.com", "Av. Siempre Viva 123",
                null, "Metropolitana", "Santiago", null,
                List.of(new ItemCompraRequest(1L, 2))
        );

        BoletaResponse res = boletaService.registrarCompra(request, 5L);

        assertEquals(30000, res.total());
        assertEquals(5L, res.usuarioId());
        verify(boletaRepository, times(1)).save(any(Boleta.class));
    }

    @Test
    void compraSinStockSuficienteLanzaExcepcionYNoGuardaBoleta() {
        ProductoExternoDTO producto = new ProductoExternoDTO(1L, "Alimento Gato", 18000, null, false, 1);

        when(productosRestClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri("/api/v1/productos/{id}", 1L)).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ProductoExternoDTO.class)).thenReturn(producto);

        when(productosRestClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri("/api/v1/productos/stock/descontar")).thenReturn(postBodySpec);
        when(postBodySpec.header(any(), any())).thenReturn(postBodySpec);
        when(postBodySpec.body(any(List.class))).thenReturn(postBodySpec);
        when(postBodySpec.retrieve()).thenThrow(
                HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8)
        );

        CompraRequest request = new CompraRequest(
                "Ana", "Lopez", "ana@mail.com", "Calle 1",
                null, "Metropolitana", "Providencia", null,
                List.of(new ItemCompraRequest(1L, 5))
        );

        assertThrows(ReglaNegocioException.class, () -> boletaService.registrarCompra(request, null));
        verify(boletaRepository, never()).save(any());
    }
}