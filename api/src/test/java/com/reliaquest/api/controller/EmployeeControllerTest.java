package com.reliaquest.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        controller = new EmployeeController(employeeService, objectMapper);
    }

    @Test
    void getAllEmployees_shouldReturnList() {
        List<Employee> mockList = List.of(new Employee("1", "Alice", 50000, 30, "Engineer", "alice@example.com"));
        when(employeeService.getAllEmployees()).thenReturn(mockList);

        ResponseEntity<List<Employee>> response = controller.getAllEmployees();

        assertThat(response.getBody()).isEqualTo(mockList);
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnFilteredEmployees() {
        List<Employee> mockResults = List.of(
                new Employee("1", "Alice", 70000, 30, "Engineer", "alice@example.com"),
                new Employee("2", "Alicia", 72000, 28, "Developer", "alicia@example.com"));
        when(employeeService.searchEmployeesByName("Ali")).thenReturn(mockResults);

        ResponseEntity<List> response = controller.getEmployeesByNameSearch("Ali");

        assertThat(response.getBody()).isEqualTo(mockResults);
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        String id = UUID.randomUUID().toString();
        Employee mockEmp = new Employee(id, "Bob", 70000, 35, "Manager", "bob@example.com");
        when(employeeService.getEmployeeById(id)).thenReturn(mockEmp);

        ResponseEntity<Employee> response = controller.getEmployeeById(id);

        assertThat(response.getBody()).isEqualTo(mockEmp);
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnValue() {
        when(employeeService.getHighestSalary()).thenReturn(100000);

        ResponseEntity<Integer> response = controller.getHighestSalaryOfEmployees();

        assertThat(response.getBody()).isEqualTo(100000);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnTop10List() {
        List<String> topTen = List.of("Alice", "Bob");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topTen);

        ResponseEntity<List<String>> response = controller.getTopTenHighestEarningEmployeeNames();

        assertThat(response.getBody()).isEqualTo(topTen);
    }

    @Test
    void createEmployee_withEmployeeObject_shouldWork() {
        Employee input = new Employee(null, "New User", 60000, 25, "Dev", "dev@example.com");
        Employee saved = new Employee(
                "generated-id",
                input.employeeName(),
                input.employeeSalary(),
                input.employeeAge(),
                input.employeeTitle(),
                input.employeeEmail());
        when(employeeService.createEmployee(input)).thenReturn(saved);

        ResponseEntity<?> response = controller.createEmployee(input);

        assertThat(response.getBody()).isEqualTo(saved);
    }

    @Test
    void createEmployee_withRawMap_shouldConvertAndWork() {
        Map<String, Object> inputMap = Map.of(
                "employee_name", "New User",
                "employee_salary", 60000,
                "employee_age", 25,
                "employee_title", "Dev",
                "employee_email", "dev@example.com");

        Employee expected = objectMapper.convertValue(inputMap, Employee.class);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(expected);

        ResponseEntity<?> response = controller.createEmployee(inputMap);

        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void createEmployee_withInvalidInput_shouldReturnBadRequest() {
        Object invalidInput = List.of("invalid");

        ResponseEntity<?> response = controller.createEmployee(invalidInput);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).asString().contains("Unsupported input type");
    }

    @Test
    void deleteEmployeeById_shouldReturnName() {
        String id = UUID.randomUUID().toString();
        when(employeeService.deleteEmployeeById(id)).thenReturn("Deleted User");

        ResponseEntity<String> response = controller.deleteEmployeeById(id);

        assertThat(response.getBody()).isEqualTo("Deleted User");
    }
}
