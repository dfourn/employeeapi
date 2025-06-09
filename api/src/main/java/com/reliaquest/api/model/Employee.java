package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Employee(
        String id,
        @JsonProperty("employee_name") String employeeName,
        @JsonProperty("employee_salary") int employeeSalary,
        @JsonProperty("employee_age") int employeeAge,
        @JsonProperty("employee_title") String employeeTitle,
        @JsonProperty("employee_email") String employeeEmail) {}
