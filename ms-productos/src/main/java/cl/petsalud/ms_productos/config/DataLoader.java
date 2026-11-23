package cl.petsalud.ms_productos.config;

import cl.petsalud.ms_productos.model.Categoria;
import cl.petsalud.ms_productos.model.Producto;
import cl.petsalud.ms_productos.repository.CategoriaRepository;
import cl.petsalud.ms_productos.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }

        Categoria perros = categoriaRepository.save(
                Categoria.builder().nombre("Perros").descripcion("Alimentos para perros").build()
        );
        Categoria gatos = categoriaRepository.save(
                Categoria.builder().nombre("Gatos").descripcion("Alimentos para gatos").build()
        );

        productoRepository.saveAll(List.of(
                crearProducto("PROD-001", "ALFA DOG", "Alimento completo para perros adultos.", 22990, 19990, true, 25, 5, "/assets/producto_1.png", perros),
                crearProducto("PROD-002", "PEDIGREE", "Pedigree adulto con carne y vegetales.", 24500, null, false, 4, 5, "/assets/producto_3.png", perros),
                crearProducto("PROD-003", "CATCHOW", "CatChow cuidado urinario para gatos.", 19990, 16990, true, 20, 5, "/assets/producto_6.png", gatos),
                crearProducto("PROD-004", "WHISKAS", "Whiskas sabor pescado para gatos.", 17500, null, false, 3, 5, "/assets/producto_7.png", gatos)
        ));
    }

    private Producto crearProducto(
            String codigo, String nombre, String descripcion,
            Integer precio, Integer precioOferta, boolean enOferta,
            Integer stock, Integer stockCritico, String imagenUrl, Categoria categoria) {
        return Producto.builder()
                .codigo(codigo)
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .precioOferta(precioOferta)
                .enOferta(enOferta)
                .stock(stock)
                .stockCritico(stockCritico)
                .imagenUrl(imagenUrl)
                .categoria(categoria)
                .build();
    }
}