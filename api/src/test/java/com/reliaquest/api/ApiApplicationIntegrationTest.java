package com.reliaquest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

/**
 * Mock Server must be running for these to pass.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE_URL = "/employee";

    @Test
    void getAllEmployees_shouldReturnList() {
        ResponseEntity<Employee[]> response = restTemplate.getForEntity(BASE_URL, Employee[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnList() {
        ResponseEntity<List<String>> response = restTemplate.exchange(
                BASE_URL + "/topTenHighestEarningEmployeeNames",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<String> names = response.getBody();
        assertThat(names).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void getHighestSalaryOfEmployees_shouldMatchMaxSalaryFromEmployeeList() {
        // Step 1: Get all employees
        ResponseEntity<Employee[]> response = restTemplate.getForEntity(BASE_URL, Employee[].class);

        assertThat(response.getStatusCode())
                .withFailMessage("Expected HTTP 200 when fetching employees but got %s", response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        List<Employee> employees = Arrays.stream(response.getBody()).toList();
        assertThat(employees)
                .withFailMessage("Expected non-empty employee list")
                .isNotNull()
                .isNotEmpty();

        // Step 2: Compute the max salary locally
        int expectedMaxSalary = employees.stream()
                .mapToInt(Employee::employeeSalary)
                .max()
                .orElseThrow(() -> new AssertionError("No salaries found"));

        // Step 3: Call /highestSalary endpoint
        ResponseEntity<Integer> salaryResponse = restTemplate.getForEntity(BASE_URL + "/highestSalary", Integer.class);

        assertThat(salaryResponse.getStatusCode())
                .withFailMessage(
                        "Expected HTTP 200 when fetching highest salary but got %s", salaryResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        Integer actualHighestSalary = salaryResponse.getBody();
        assertThat(actualHighestSalary)
                .withFailMessage("Expected highest salary to be %d but got %d", expectedMaxSalary, actualHighestSalary)
                .isEqualTo(expectedMaxSalary);
    }

    @Test
    void searchEmployeesByName_shouldReturnExactMatches() throws InterruptedException {
        Employee alice = new Employee(null, "Alice Wonderland", 90000, 28, "Engineer", "alice@example.com");
        restTemplate.postForEntity(BASE_URL, alice, Employee.class);

        Thread.sleep(500);

        ResponseEntity<Employee[]> response = restTemplate.getForEntity(BASE_URL + "/search/Alice", Employee[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Employee[] matches = response.getBody();
        assertThat(matches).isNotEmpty();
        assertThat(matches[0].employeeName()).contains("Alice");
    }

    @Test
    void createAndDeleteEmployee_shouldWork() {
        Employee newEmp = new Employee(null, "Test User", 123000, 30, "QA Engineer", "testuser@example.com");

        // create
        ResponseEntity<Employee> createResponse = restTemplate.postForEntity(BASE_URL, newEmp, Employee.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        String createdId = createResponse.getBody().id();

        // delete
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("name", createResponse.getBody().employeeName());

        HttpEntity<Map<String, String>> deleteRequest = new HttpEntity<>(body, headers);
        ResponseEntity<String> deleteResponse =
                restTemplate.exchange(BASE_URL + "/" + createdId, HttpMethod.DELETE, deleteRequest, String.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).isEqualTo("Test User");
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        Employee bob = new Employee(null, "Bob Builder", 80000, 35, "Builder", "bob@example.com");
        ResponseEntity<Employee> created = restTemplate.postForEntity(BASE_URL, bob, Employee.class);

        String id = created.getBody().id();
        ResponseEntity<Employee> response = restTemplate.getForEntity(BASE_URL + "/" + id, Employee.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().employeeName()).isEqualTo("Bob Builder");
    }

    @Test
    void getEmployeeById_withInvalidId_shouldReturnNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/nonexistent-id", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
