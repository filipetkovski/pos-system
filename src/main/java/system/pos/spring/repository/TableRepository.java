package system.pos.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import system.pos.spring.model.Tables;

@Repository
public interface TableRepository extends JpaRepository<Tables, Long> {
    boolean existsByNumber(long number);
}
