package com.reliaquest.api.service.client;

import com.reliaquest.api.model.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServerClient {

    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";
    private static final int RETRYABLE_MAX_ATTEMPTS = 9;

    private final RestTemplate restTemplate;

    @Autowired
    public ServerClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(maxAttempts = RETRYABLE_MAX_ATTEMPTS, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Cacheable("employees")
    public List<Employee> fetchAllEmployees() {
        ResponseEntity<EmployeeListResponse> response =
                restTemplate.exchange(BASE_URL, HttpMethod.GET, null, EmployeeListResponse.class);
        return Optional.ofNullable(response.getBody())
                .map(EmployeeListResponse::data)
                .orElse(List.of());
    }

    @Retryable(maxAttempts = RETRYABLE_MAX_ATTEMPTS, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Cacheable(value = "employeeById", key = "#id")
    public Employee fetchEmployeeById(String id) {
        String url = BASE_URL + "/" + id;
        ResponseEntity<EmployeeResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);
        return Optional.ofNullable(response.getBody())
                .map(EmployeeResponse::data)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
    }

    @Retryable(maxAttempts = RETRYABLE_MAX_ATTEMPTS, backoff = @Backoff(delay = 1000, multiplier = 2))
    @CachePut(value = "employeeById", key = "#result.id")
    public Employee createEmployee(Employee employee) {
        CreateMockEmployeeInput input = new CreateMockEmployeeInput(
                employee.employeeName(), employee.employeeSalary(),
                employee.employeeAge(), employee.employeeTitle());

        HttpEntity<CreateMockEmployeeInput> request = new HttpEntity<>(input);
        ResponseEntity<EmployeeResponse> response =
                restTemplate.exchange(BASE_URL, HttpMethod.POST, request, EmployeeResponse.class);

        return Optional.ofNullable(response.getBody())
                .map(EmployeeResponse::data)
                .orElseThrow(() -> new RuntimeException("Employee creation failed"));
    }

    @Retryable(maxAttempts = RETRYABLE_MAX_ATTEMPTS, backoff = @Backoff(delay = 1000, multiplier = 2))
    @CacheEvict(value = "employeeById", key = "#employee.id")
    public void deleteEmployee(Employee employee) {
        DeleteMockEmployeeInput input = new DeleteMockEmployeeInput(employee.employeeName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeleteMockEmployeeInput> request = new HttpEntity<>(input, headers);

        restTemplate.exchange(BASE_URL, HttpMethod.DELETE, request, String.class);
    }
}
