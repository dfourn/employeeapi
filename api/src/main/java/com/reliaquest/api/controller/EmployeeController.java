package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeController implements IEmployeeController {

    private final EmployeeService employeeService;

    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List> getEmployeesByNameSearch(String searchString) {
        return ResponseEntity.ok(employeeService.searchEmployeesByName(searchString));
    }

    @Override
    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<Employee> getEmployeeById(String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(employeeService.getHighestSalary());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTop10HighestEarningEmployeeNames());
    }

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEmployee(@RequestBody Object employeeInput) {
        Employee employee;

        if (employeeInput instanceof Employee e) {
            employee = e;
        } else if (employeeInput instanceof Map<?, ?> rawMap) {
            try {
                employee = objectMapper.convertValue(rawMap, Employee.class);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("Invalid input format: " + ex.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Unsupported input type: must be Employee or JSON object");
        }

        Employee created = employeeService.createEmployee(employee);
        return ResponseEntity.ok(created);
    }

    @Override
    @DeleteMapping("/{id:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<String> deleteEmployeeById(String id) {
        return ResponseEntity.ok(employeeService.deleteEmployeeById(id));
    }
}
