package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.ServerClient;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final ServerClient serverClient;

    @Autowired
    public EmployeeService(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public List<Employee> getAllEmployees() {
        return serverClient.fetchAllEmployees();
    }

    public List<Employee> searchEmployeesByName(String name) {
        return getAllEmployees().stream()
                .filter(emp -> emp.employeeName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Employee getEmployeeById(String id) {
        return serverClient.fetchEmployeeById(id);
    }

    public int getHighestSalary() {
        return getAllEmployees().stream()
                .mapToInt(Employee::employeeSalary)
                .max()
                .orElse(0);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::employeeSalary).reversed())
                .limit(10)
                .map(Employee::employeeName)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(Employee employee) {
        // TODO: Handle duplicates
        return serverClient.createEmployee(employee);
    }

    public String deleteEmployeeById(String id) {
        Employee target = getEmployeeById(id);
        serverClient.deleteEmployee(target);
        return target.employeeName();
    }
}
