package system.pos.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddedProductRepository extends JpaRepository<system.pos.spring.model.AddedProduct, Long> {
}
