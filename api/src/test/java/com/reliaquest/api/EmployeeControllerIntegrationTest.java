package com.reliaquest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerIntegrationTest {

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
    void getTop10HighestEarningEmployeeNames_shouldReturnList() {
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
    void getHighestSalaryOfEmployees_shouldReturnInt() {
        ResponseEntity<Integer> response = restTemplate.getForEntity(BASE_URL + "/highestSalary", Integer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isGreaterThan(0);
    }

    @Test
    void searchEmployeesByName_shouldReturnMatches() {
        String nameFragment = "a";
        var response = restTemplate.getForEntity(BASE_URL + "/search/" + nameFragment, Employee[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
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
}
