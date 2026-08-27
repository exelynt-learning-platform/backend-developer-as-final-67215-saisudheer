package com.resourcebooking.service;

import com.resourcebooking.dto.resource.ResourceRequest;
import com.resourcebooking.dto.resource.ResourceResponse;
import com.resourcebooking.entity.Resource;
import com.resourcebooking.exception.ResourceNotFoundException;
import com.resourcebooking.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public ResourceResponse createResource(ResourceRequest request) {

        Resource resource = new Resource();

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());

        if (request.getAvailable() == null) {
            resource.setAvailable(true);
        } else {
            resource.setAvailable(request.getAvailable());
        }

        Resource savedResource = resourceRepository.save(resource);

        return mapToResponse(savedResource);
    }

    public List<ResourceResponse> getAllResources() {

        return resourceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ResourceResponse getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        return mapToResponse(resource);
    }

    public ResourceResponse updateResource(
            Long id,
            ResourceRequest request) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());

        if (request.getAvailable() != null) {
            resource.setAvailable(request.getAvailable());
        }

        Resource updatedResource = resourceRepository.save(resource);

        return mapToResponse(updatedResource);
    }

    public void deleteResource(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        resourceRepository.delete(resource);
    }

    private ResourceResponse mapToResponse(Resource resource) {

        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.getAvailable(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}