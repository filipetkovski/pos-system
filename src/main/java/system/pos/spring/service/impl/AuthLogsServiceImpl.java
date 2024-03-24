package system.pos.spring.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import system.pos.spring.model.AuthLog;
import system.pos.spring.model.Employee;
import system.pos.spring.repository.AuthLogsRepository;
import system.pos.spring.service.AuthLogsService;

import java.util.List;

@Service
public class AuthLogsServiceImpl implements AuthLogsService {
    private final AuthLogsRepository authLogsRepository;

    public AuthLogsServiceImpl(AuthLogsRepository authLogsRepository) {
        this.authLogsRepository = authLogsRepository;
    }

    @Override
    public void save(Employee employee, String status) {
        authLogsRepository.save(new AuthLog(employee.getName(),status));
    }

    @Override
    public List<AuthLog> findAll() {
        return authLogsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
}
