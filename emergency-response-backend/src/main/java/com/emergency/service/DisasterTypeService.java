package com.emergency.service;

import com.emergency.dto.request.DisasterTypeRequest;
import com.emergency.dto.response.DisasterTypeResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.DisasterType;
import com.emergency.repository.DisasterTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisasterTypeService {

    @Autowired
    private DisasterTypeRepository disasterTypeRepository;

    public List<DisasterTypeResponse> getAll() {
        return disasterTypeRepository.findAllByOrderByNameAsc().stream()
            .map(DisasterTypeResponse::fromEntity)
            .toList();
    }

    public DisasterTypeResponse getById(Long id) {
        DisasterType dt = disasterTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Disaster type not found"));
        return DisasterTypeResponse.fromEntity(dt);
    }

    public DisasterTypeResponse create(DisasterTypeRequest request) {
        if (disasterTypeRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        DisasterType dt = new DisasterType();
        dt.setName(request.name());
        dt.setSlug(request.slug());
        dt.setIcon(request.icon());
        dt.setBaseUrgencyScore(request.baseUrgencyScore());

        return DisasterTypeResponse.fromEntity(disasterTypeRepository.save(dt));
    }

    public DisasterTypeResponse update(Long id, DisasterTypeRequest request) {
        DisasterType dt = disasterTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Disaster type not found"));

        if (!dt.getSlug().equals(request.slug()) && disasterTypeRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        dt.setName(request.name());
        dt.setSlug(request.slug());
        dt.setIcon(request.icon());
        dt.setBaseUrgencyScore(request.baseUrgencyScore());

        return DisasterTypeResponse.fromEntity(disasterTypeRepository.save(dt));
    }

    public void delete(Long id) {
        if (!disasterTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disaster type not found");
        }
        disasterTypeRepository.deleteById(id);
    }
}
