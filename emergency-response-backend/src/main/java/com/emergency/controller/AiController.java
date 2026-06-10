package com.emergency.controller;

import com.emergency.dto.request.ChatRequest;
import com.emergency.dto.response.ChatResponse;
import com.emergency.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = aiService.respond(request.message(), request.callId(), request.history());
        return ResponseEntity.ok(response);
    }
}
