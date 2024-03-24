package system.pos.spring.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import system.pos.spring.enumm.ProductType;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long code;
    private String name;
    private Integer price;
    @Enumerated(EnumType.STRING)
    private ProductType type;
    private boolean visible;
    private byte[] image;
    @ManyToOne()
    private Category category;

    public Product(String name, Integer price, ProductType type,byte[] image, Category category) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.image = image;
        this.category = category;
        this.visible = true;
    }
}
