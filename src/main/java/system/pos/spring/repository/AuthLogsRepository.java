package system.pos.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import system.pos.spring.model.AuthLog;

@Repository
public interface AuthLogsRepository extends JpaRepository<AuthLog, Long> {
}
