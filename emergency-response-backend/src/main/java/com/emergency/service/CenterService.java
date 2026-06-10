package com.emergency.service;

import com.emergency.dto.request.CenterRequest;
import com.emergency.dto.response.CenterResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.RescueCenter;
import com.emergency.repository.RescueCenterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CenterService {

    private final RescueCenterRepository repository;

    public CenterService(RescueCenterRepository repository) {
        this.repository = repository;
    }

    public List<CenterResponse> getAll(String type) {
        if (type != null && !type.isBlank()) {
            RescueCenter.CenterType ct = RescueCenter.CenterType.valueOf(type);
            return repository.findByType(ct).stream()
                .map(CenterResponse::fromEntity)
                .toList();
        }
        return repository.findAllByOrderByNameAsc().stream()
            .map(CenterResponse::fromEntity)
            .toList();
    }

    public CenterResponse getById(Long id) {
        RescueCenter c = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rescue center not found"));
        return CenterResponse.fromEntity(c);
    }

    public CenterResponse create(CenterRequest request) {
        if (repository.findByNameOrderByIdAsc(request.name()).stream().findAny().isPresent()) {
            throw new IllegalArgumentException("A rescue center with this name already exists");
        }
        RescueCenter c = new RescueCenter();
        c.setName(request.name());
        c.setType(RescueCenter.CenterType.valueOf(request.type()));
        c.setLat(request.lat());
        c.setLng(request.lng());
        c.setAddress(request.address());
        c.setPhone(request.phone());
        c.setSupplies(request.supplies());
        c.setCapacity(request.capacity());
        return CenterResponse.fromEntity(repository.save(c));
    }

    public CenterResponse update(Long id, CenterRequest request) {
        RescueCenter c = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rescue center not found"));
        if (!c.getName().equals(request.name()) &&
            repository.findByNameOrderByIdAsc(request.name()).stream().findAny().isPresent()) {
            throw new IllegalArgumentException("A rescue center with this name already exists");
        }
        c.setName(request.name());
        c.setType(RescueCenter.CenterType.valueOf(request.type()));
        c.setLat(request.lat());
        c.setLng(request.lng());
        c.setAddress(request.address());
        c.setPhone(request.phone());
        c.setSupplies(request.supplies());
        c.setCapacity(request.capacity());
        return CenterResponse.fromEntity(repository.save(c));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Rescue center not found");
        }
        repository.deleteById(id);
    }
}
