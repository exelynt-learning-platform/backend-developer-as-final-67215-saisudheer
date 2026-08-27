package com.resourcebooking.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    @Size(
            max = 150,
            message = "Resource name must not exceed 150 characters"
    )
    private String name;

    @NotBlank(message = "Resource description is required")
    @Size(
            max = 500,
            message = "Resource description must not exceed 500 characters"
    )
    private String description;

    @NotBlank(message = "Resource type is required")
    @Size(
            max = 100,
            message = "Resource type must not exceed 100 characters"
    )
    private String type;

    private Boolean available;

    public ResourceRequest() {
    }

    public ResourceRequest(
            String name,
            String description,
            String type,
            Boolean available) {

        this.name = name;
        this.description = description;
        this.type = type;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}