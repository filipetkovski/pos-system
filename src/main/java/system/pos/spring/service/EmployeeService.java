package system.pos.spring.service;

import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.Employee;

import java.util.List;

public interface EmployeeService {

    Employee checkLogin(long code);

    void updateEmployee(Employee employee);

    Employee isValidCode(Long code, String name, UserRole role);

    List<Employee> findAll();

    List<Employee> findActiveEmployees();

    void deleteEmployee(Employee employee);

    Employee findByCode(Long code);

    void save(Employee employee);
}
