package com.resourcebooking.controller;

import com.resourcebooking.dto.resource.ResourceRequest;
import com.resourcebooking.dto.resource.ResourceResponse;
import com.resourcebooking.service.ResourceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * USER + ADMIN
     *
     * Get all resources.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ResourceResponse>> getAllResources() {

        List<ResourceResponse> resources =
                resourceService.getAllResources();

        return ResponseEntity.ok(resources);
    }

    /**
     * USER + ADMIN
     *
     * Get resource by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable Long id) {

        ResourceResponse resource =
                resourceService.getResourceById(id);

        return ResponseEntity.ok(resource);
    }

    /**
     * ADMIN ONLY
     *
     * Create resource.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> createResource(
            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                resourceService.createResource(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * ADMIN ONLY
     *
     * Update resource.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                resourceService.updateResource(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN ONLY
     *
     * Delete resource.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id) {

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }
}