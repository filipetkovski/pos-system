package system.pos.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_product")
public class AddedProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    Order order;
    Integer quantity;
    String description;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    Product product;

    public AddedProduct(Order order, Integer quantity, String description, Product product) {
        this.order = order;
        this.quantity = quantity;
        this.description = description;
        this.product = product;
    }
}
