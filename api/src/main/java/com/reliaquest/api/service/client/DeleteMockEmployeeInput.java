package com.reliaquest.api.service.client;

import lombok.Data;

@Data
public class DeleteMockEmployeeInput {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeleteMockEmployeeInput(String name) {
        this.name = name;
    }
}
