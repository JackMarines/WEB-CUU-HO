package com.emergency.controller;

import com.emergency.dto.request.CenterRequest;
import com.emergency.dto.response.CenterResponse;
import com.emergency.service.CenterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centers")
public class CenterController {

    private final CenterService centerService;

    public CenterController(CenterService centerService) {
        this.centerService = centerService;
    }

    @GetMapping
    public ResponseEntity<List<CenterResponse>> getAll(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(centerService.getAll(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CenterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(centerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CenterResponse> create(@Valid @RequestBody CenterRequest request) {
        return ResponseEntity.ok(centerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CenterResponse> update(@PathVariable Long id, @Valid @RequestBody CenterRequest request) {
        return ResponseEntity.ok(centerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        centerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
