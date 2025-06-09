package com.reliaquest.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.config.CacheTestConfig;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.api.service.client.ServerClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Import(CacheTestConfig.class)
@EnableCaching
class CacheIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @SpyBean
    private RestTemplate restTemplate;

    @SpyBean
    private ServerClient serverClient;

    @Test
    void fetchEmployeeById_shouldReturnEmployeeAndCacheIt() {
        String id = UUID.randomUUID().toString();
        Employee expected = new Employee(id, "Cache User", 70000, 28, "Engineer", "cache@example.com");
        EmployeeResponse employeeResponse = new EmployeeResponse(expected, "success");

        ResponseEntity<EmployeeResponse> mockResponse = ResponseEntity.ok(employeeResponse);

        // Correct mock for RestTemplate.exchange(...) with explicit HttpMethod
        doReturn(mockResponse)
                .when(restTemplate)
                .exchange(
                        eq("http://localhost:8112/api/v1/employee/" + id),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(EmployeeResponse.class));

        // First call
        Employee first = serverClient.fetchEmployeeById(id);
        assertThat(first).isEqualTo(expected);

        // Second call (if caching is active, restTemplate.exchange shouldn't be hit again)
        Employee second = serverClient.fetchEmployeeById(id);
        assertThat(second).isEqualTo(expected);

        // Verify restTemplate called once due to cache
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(EmployeeResponse.class));
    }
}
