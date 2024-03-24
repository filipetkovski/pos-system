package system.pos.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import system.pos.spring.model.Log;

@Repository
public interface LogsRepository extends JpaRepository<Log, Long> {
}
