package system.pos.spring.service.impl;

import org.springframework.stereotype.Service;
import system.pos.spring.enumm.EmployeeStatus;
import system.pos.spring.enumm.UserRole;
import system.pos.spring.model.Employee;
import system.pos.spring.repository.EmployeeRepository;
import system.pos.spring.service.EmployeeService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee checkLogin(long code) {
        return employeeRepository.findByCode(code);
    }

    @Override
    public void updateEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    @Override
    public Employee isValidCode(Long code, String name, UserRole role) {
        if(!employeeRepository.existsByCode(code)) {
            return employeeRepository.save(new Employee(code,name,EmployeeStatus.НЕАКТИВЕН,role,new ArrayList<>()));
        }
        return null;
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public List<Employee> findActiveEmployees() {
        return employeeRepository.findAll().stream().filter(employee -> employee.getStatus().equals(EmployeeStatus.АКТИВЕН)).collect(Collectors.toList());
    }

    @Override
    public void deleteEmployee(Employee employee) {
        employeeRepository.delete(employee);
    }

    @Override
    public Employee findByCode(Long code) {
        return employeeRepository.findByCode(code);
    }

    @Override
    public void save(Employee employee) {
        employeeRepository.save(employee);
    }
}
