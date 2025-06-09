package com.reliaquest.api.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ServerClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ServerClient serverClient;

    private Employee mockEmployee;

    @BeforeEach
    void setup() {
        mockEmployee = new Employee(UUID.randomUUID().toString(), "Alice", 75000, 29, "Engineer", "alice@example.com");
    }

    @Test
    void fetchAllEmployees_shouldReturnEmployeeList() {
        EmployeeListResponse response = new EmployeeListResponse(List.of(mockEmployee), "success");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(EmployeeListResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        List<Employee> result = serverClient.fetchAllEmployees();

        assertThat(result).containsExactly(mockEmployee);
    }

    @Test
    void fetchAllEmployees_shouldReturnEmptyListWhenResponseBodyNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(EmployeeListResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Employee> result = serverClient.fetchAllEmployees();

        assertThat(result).isEmpty();
    }

    @Test
    void fetchEmployeeById_shouldReturnEmployee() {
        String id = mockEmployee.id();
        EmployeeResponse response = new EmployeeResponse(mockEmployee, "success");
        when(restTemplate.exchange(contains(id), eq(HttpMethod.GET), isNull(), eq(EmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        Employee result = serverClient.fetchEmployeeById(id);

        assertThat(result).isEqualTo(mockEmployee);
    }

    @Test
    void fetchEmployeeById_shouldThrowWhenMissing() {
        String id = "missing-id";
        when(restTemplate.exchange(contains(id), eq(HttpMethod.GET), isNull(), eq(EmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> serverClient.fetchEmployeeById(id))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        CreateMockEmployeeInput expectedInput = new CreateMockEmployeeInput(
                mockEmployee.employeeName(),
                mockEmployee.employeeSalary(),
                mockEmployee.employeeAge(),
                mockEmployee.employeeTitle());

        EmployeeResponse response = new EmployeeResponse(mockEmployee, "success");

        when(restTemplate.exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(EmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        Employee result = serverClient.createEmployee(mockEmployee);

        assertThat(result).isEqualTo(mockEmployee);
    }

    @Test
    void deleteEmployee_shouldSendDeleteRequest() {
        DeleteMockEmployeeInput input = new DeleteMockEmployeeInput(mockEmployee.employeeName());

        serverClient.deleteEmployee(mockEmployee);

        verify(restTemplate)
                .exchange(
                        eq("http://localhost:8112/api/v1/employee"),
                        eq(HttpMethod.DELETE),
                        argThat(request -> {
                            DeleteMockEmployeeInput body = (DeleteMockEmployeeInput) request.getBody();
                            return body.getName().equals(mockEmployee.employeeName());
                        }),
                        eq(String.class));
    }
}
