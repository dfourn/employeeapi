package com.reliaquest.api.service.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateMockEmployeeInput {

    private String name;

    private Integer salary;

    private Integer age;

    private String title;
}
