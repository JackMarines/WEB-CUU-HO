package com.emergency.controller;

import com.emergency.dto.request.DisasterTypeRequest;
import com.emergency.dto.response.DisasterTypeResponse;
import com.emergency.service.DisasterTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disaster-types")
public class DisasterTypeController {

    @Autowired
    private DisasterTypeService disasterTypeService;

    @GetMapping
    public ResponseEntity<List<DisasterTypeResponse>> getAll() {
        return ResponseEntity.ok(disasterTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisasterTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(disasterTypeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DisasterTypeResponse> create(@Valid @RequestBody DisasterTypeRequest request) {
        return ResponseEntity.ok(disasterTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisasterTypeResponse> update(@PathVariable Long id, @Valid @RequestBody DisasterTypeRequest request) {
        return ResponseEntity.ok(disasterTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        disasterTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
