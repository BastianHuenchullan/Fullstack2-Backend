package cl.petsalud.ms_productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer precio;

    @Column(name = "precio_oferta")
    private Integer precioOferta;

    @Builder.Default
    @Column(name = "en_oferta", nullable = false)
    private boolean enOferta = false;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_critico", nullable = false)
    private Integer stockCritico;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
