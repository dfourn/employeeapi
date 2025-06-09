package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.client.ServerClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class EmployeeServiceTest {

    @Mock
    private ServerClient serverClient;

    @InjectMocks
    private EmployeeService employeeService;

    private final List<Employee> sampleEmployees = List.of(
            new Employee("1", "Alice", 100000, 30, "Engineer", "alice@example.com"),
            new Employee("2", "Bob", 120000, 40, "Manager", "bob@example.com"),
            new Employee("3", "Charlie", 90000, 25, "Dev", "charlie@example.com"));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllEmployees_shouldReturnList() {
        when(serverClient.fetchAllEmployees()).thenReturn(sampleEmployees);

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).isEqualTo(sampleEmployees);
    }

    @Test
    void searchEmployeesByName_shouldFilterByName() {
        when(serverClient.fetchAllEmployees()).thenReturn(sampleEmployees);

        List<Employee> result = employeeService.searchEmployeesByName("a"); // matches Alice & Charlie

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::employeeName).containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void getEmployeeById_shouldReturnCorrectEmployee() {
        String id = UUID.randomUUID().toString();
        Employee expected = new Employee(id, "Test User", 80000, 32, "QA", "test@example.com");
        when(serverClient.fetchEmployeeById(id)).thenReturn(expected);

        Employee result = employeeService.getEmployeeById(id);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getHighestSalary_shouldReturnMax() {
        when(serverClient.fetchAllEmployees()).thenReturn(sampleEmployees);

        int result = employeeService.getHighestSalary();

        assertThat(result).isEqualTo(120000);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnSortedNames() {
        when(serverClient.fetchAllEmployees()).thenReturn(sampleEmployees);

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertThat(result).containsExactly("Bob", "Alice", "Charlie");
    }

    @Test
    void createEmployee_shouldDelegateToServerClient() {
        Employee input = new Employee(null, "New Hire", 95000, 26, "Engineer", "new@example.com");
        Employee created = new Employee(
                "123",
                input.employeeName(),
                input.employeeSalary(),
                input.employeeAge(),
                input.employeeTitle(),
                input.employeeEmail());

        when(serverClient.createEmployee(input)).thenReturn(created);

        Employee result = employeeService.createEmployee(input);

        assertThat(result).isEqualTo(created);
    }

    @Test
    void deleteEmployeeById_shouldCallDeleteAndReturnName() {
        String id = "abc123";
        Employee employee = new Employee(id, "To Delete", 65000, 33, "Ops", "del@example.com");

        when(serverClient.fetchEmployeeById(id)).thenReturn(employee);

        String result = employeeService.deleteEmployeeById(id);

        verify(serverClient).deleteEmployee(employee);
        assertThat(result).isEqualTo("To Delete");
    }
}
