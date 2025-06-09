package com.reliaquest.api.model;

import java.util.List;

public record EmployeeListResponse(List<Employee> data, String status) {}
