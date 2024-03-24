package system.pos.spring.service;

import system.pos.spring.model.AuthLog;
import system.pos.spring.model.Employee;

import java.util.List;

public interface AuthLogsService {
    void save(Employee employee, String status);

    List<AuthLog> findAll();
}
