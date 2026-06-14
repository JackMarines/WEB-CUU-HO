package com.emergency.controller;

import com.emergency.dto.request.CallStatusRequest;
import com.emergency.dto.request.DistressCallRequest;
import com.emergency.dto.request.FeedbackRequest;
import com.emergency.dto.request.ResponseRequest;
import com.emergency.dto.response.DistressCallResponse;
import com.emergency.dto.response.ResponseResponse;
import com.emergency.service.CallService;
import com.emergency.service.ResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallController {

    @Autowired
    private CallService callService;

    @Autowired
    private ResponseService responseService;

    @GetMapping
    public ResponseEntity<List<DistressCallResponse>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        return ResponseEntity.ok(callService.getAll(type, status, q, dateFrom, dateTo));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DistressCallResponse>> getMine(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(callService.getMine(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistressCallResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(callService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DistressCallResponse> create(@Valid @RequestBody DistressCallRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (auth != null && auth.getPrincipal() instanceof Long) {
            userId = (Long) auth.getPrincipal();
        }
        return ResponseEntity.ok(callService.create(request, userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DistressCallResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CallStatusRequest request) {
        return ResponseEntity.ok(callService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCall(@PathVariable Long id) {
        callService.deleteCall(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(callService.getDashboardStats());
    }

    @GetMapping("/{callId}/responses")
    public ResponseEntity<List<ResponseResponse>> getResponses(@PathVariable Long callId) {
        return ResponseEntity.ok(responseService.getByCallId(callId));
    }

    @PostMapping("/{callId}/responses")
    public ResponseEntity<ResponseResponse> assignResponse(
            @PathVariable Long callId,
            @Valid @RequestBody ResponseRequest request,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(responseService.assign(callId, request, userId));
    }

    @PutMapping("/responses/{id}/status")
    public ResponseEntity<ResponseResponse> updateResponseStatus(
            @PathVariable Long id,
            @Valid @RequestBody CallStatusRequest request) {
        return ResponseEntity.ok(responseService.updateStatus(id, request.status()));
    }

    @PostMapping("/{callId}/feedback")
    public ResponseEntity<ResponseResponse> submitFeedback(
            @PathVariable Long callId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(responseService.submitFeedback(callId, request, userId));
    }
}
