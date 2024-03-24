package system.pos.spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    @ManyToOne
    Category supercategory;
    Integer level;
    boolean visible;
    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    List<Product> products;

    public Category(String name, Category supercategory, Integer level) {
        this.name = name;
        this.supercategory = supercategory;
        this.level = level;
        this.products = new ArrayList<>();
        this.visible = true;
    }
}
