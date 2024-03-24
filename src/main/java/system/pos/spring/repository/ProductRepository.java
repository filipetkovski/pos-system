package system.pos.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import system.pos.spring.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
     boolean existsByName(String name);
}
